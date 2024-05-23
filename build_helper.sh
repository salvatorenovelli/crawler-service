#!/bin/bash



echo "Fetching gradle project properties..."
IFS=':' read -ra props_array <<< "$(./gradlew -q getProjectProperties)"

export GCE_PROJECT_ID="$(gcloud config get-value project -q)"
export GROUP_ID=${props_array[0]}
export ARTIFACT_ID=${props_array[1]}
export VERSION=${props_array[2]}

export IMAGE_TAG=eu.gcr.io/${GCE_PROJECT_ID}/${ARTIFACT_ID}:${VERSION}


echo Building version ${VERSION}

if [[ -z "$1" ]]
  then
    echo "No argument supplied"
    exit
fi

echo "Initiating $1 ${ARTIFACT_ID} version ${props_array[2]}"


confirm() {
    read -r -p "Do you want to continue? [y/N]" response
    case "$response" in
        [yY][eE][sS]|[yY])
            true
            ;;
        *)
            false
            ;;
    esac
}

RED='\033[1;31m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NO_COLOR='\033[0m'


# Evaluates the variables that will be injected into google cloud build
function evaluateSubstitutions() {
    local GIT_TAG_NAME=$(getGitTagName)
    local GIT_SHORT_SHA=$(git rev-parse --short HEAD)
    local LOCAL_ID=$(whoami)_$(hostname)

    local TAG_NAME=${LOCAL_ID}"."${GIT_TAG_NAME}
    local SHORT_SHA=${LOCAL_ID}"."${GIT_SHORT_SHA}

    echo -e "Triggering build for:"
    echo -e " TAG: ${BLUE}${TAG_NAME}${NO_COLOR}"
    echo -e " SHA: ${BLUE}${SHORT_SHA}${NO_COLOR}"
    confirm || exit

    SUBST=TAG_NAME=${TAG_NAME},SHORT_SHA=${SHORT_SHA}
    echo $SUBST
}

case $1 in
    "cloud-build" )
        evaluateSubstitutions
        gcloud builds submit --substitutions=${SUBST} --machine-type=n1-highcpu-8
    ;;
    "cloud-build-local" )
        evaluateSubstitutions
        cloud-build-local  --substitutions=${SUBST} --dryrun=false --write-workspace=../tmp_cloudbuildlocal .
    ;;
    "build" )
        echo "Building ${IMAGE_TAG}"
        ./gradlew clean build || exit 1
        cp build/libs/*.jar docker
        docker build docker -t ${IMAGE_TAG}
    ;;
    "run" )
       docker run --rm -it \
         -e GOOGLE_APPLICATION_CREDENTIALS=/run/secrets/gcpcredentials.json -v /run/secrets/:/run/secrets/ \
         -e GOOGLE_CLOUD_PROJECT=${GCE_PROJECT_ID} \
         --network="host" ${IMAGE_TAG}
    ;;
    "push" )
        gcloud docker -- push ${IMAGE_TAG}
    ;;
    "deploy" )
        kubectl delete -f k8s/production.yaml
        sed -i.bak "s#<IMAGE_TAG_DO_NOT_EDIT>#${IMAGE_TAG}#" k8s/production.yaml
        kubectl apply -f k8s/production.yaml
        mv k8s/production.yaml.bak k8s/production.yaml
    ;;
        *)
        echo "'$1' is not a valid action"
    ;;
esac
