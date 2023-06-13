export VERSION=$(head -1 version)
echo "Building version ${VERSION}"

docker build -t dontletexpireapp:"${VERSION}" .
