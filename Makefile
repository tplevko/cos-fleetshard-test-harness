
.PHONY: docker-build
docker-build: check-env
	docker build -t $(IMG) .

.PHONY: docker-push
docker-push: check-env
	docker push $(IMG)

.PHONY: check-env
check-env:
ifeq (,$(IMG))
	$(error Please set the IMG environment variable)
endif
