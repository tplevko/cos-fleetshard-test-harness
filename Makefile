DOCKER_IMG = quay.io/abrianik/cos-fleetshard-test-harness

.PHONY: build
build:
	CGO_ENABLED=0 go test -v -c

.PHONY: docker-build
docker-build:
	docker build -t $(DOCKER_IMG) .

.PHONY: docker-push
docker-push:
	docker push $(DOCKER_IMG)
