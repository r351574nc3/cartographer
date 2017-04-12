#!/bin/bash
VERSION=0.1.0
IMAGE=nexus-chart

usage() {
  echo "Usage: $0 [-d] [-h]" 1>&2;
  echo "  -d deploy"
  echo "  -h help"
  exit 1;
}

if [ $? != 0 ] ; then usage ; fi

while getopts "hd" o; do
    case "${o}" in
        d)
         DEPLOY=true
         ;;
        h)
            usage
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

REPO="293135079892.dkr.ecr.us-west-2.amazonaws.com"
DOCKER="${REPO}/${IMAGE}:${VERSION}"

eval $(aws ecr get-login)

docker build -t ${DOCKER} .

if [ "$DEPLOY" ]; then
  docker push ${DOCKER}
fi
