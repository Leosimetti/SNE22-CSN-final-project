/* eslint-disable no-undef */
import { createContext } from "react";

import { AppClient } from "../proto/schema_grpc_web_pb";
import { APPLICATION_SERVER } from "./URLs";

const ProtoContext = createContext();
const PROTO_INFO = {
  client: new AppClient(APPLICATION_SERVER),
  shared: require("../proto/schema_grpc_web_pb"),
  schema: require("../proto/schema_pb.js"),
};

export default ProtoContext;
export { PROTO_INFO };
