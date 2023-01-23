export VERSION=$(head -1 version)
echo "Building version ${VERSION}"

sbt clean

DOCKERFILE="Dockerfile_${VERSION}"
sbt assembly &&
  envsubst < Dockerfile.tmpl > "${DOCKERFILE}" &&
  docker build -f "${DOCKERFILE}" -t dontletexpireapp:"${VERSION}" . &&
  rm "${DOCKERFILE}"
