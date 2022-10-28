./protobuf-javascript/bin/protoc ../protobuf/schema.proto \
    --plugin=/home/nikololiahim/SNE22-CSN-final-project/frontend/protobuf-javascript/bin/protoc-gen-js \
    --plugin=/home/nikololiahim/SNE22-CSN-final-project/frontend/protobuf-javascript/bin/protoc-gen-grpc-web \
    --proto_path=../protobuf \
    --js\_out=import\_style=commonjs,binary:src \
    --grpc-web\_out=import\_style=commonjs,mode=grpcwebtext:src
