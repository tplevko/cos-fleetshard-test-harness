FROM registry.access.redhat.com/ubi8/go-toolset as builder
USER root
WORKDIR /workspace
COPY . .
RUN make build

FROM registry.access.redhat.com/ubi8/ubi-minimal
ENV ACK_GINKGO_DEPRECATIONS=1.16.4
RUN mkdir /test-run-results
COPY --from=builder /workspace/cos-fleetshard-test-harness.test /
ENTRYPOINT [ "/cos-fleetshard-test-harness.test" ]
