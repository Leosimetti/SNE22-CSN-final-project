FROM python:3.10-alpine AS PROTO_BUILD

COPY protobuf/schema.proto .

RUN apk update
RUN apk add protobuf

RUN protoc --python_out=. schema.proto


FROM python:3.10-alpine

COPY --from=PROTO_BUILD schema_pb2.py ./
COPY worker/src/main.py ./
COPY worker/src/judge.py ./
COPY worker/requirements.txt ./

RUN pip install -r requirements.txt

ENTRYPOINT [ "python", "main.py" ]
