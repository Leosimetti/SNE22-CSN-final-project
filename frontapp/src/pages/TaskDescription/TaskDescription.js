import {
  Heading,
  Paragraph,
  Card,
  CardHeader,
  CardBody
} from "grommet";
import ContestNav from "../../common/ContestNav";
import "./TaskDescription.css";

export default function TaskDescription() {
  return (
    <>
      <ContestNav />
      <Heading style={{"line-height": "20px"}}>Taskname</Heading>
      <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
        tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim
        veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea
        commodo consequat. Duis aute irure dolor in reprehenderit in voluptate
        velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint
        occaecat cupidatat non proident, sunt in culpa qui officia deserunt
        mollit anim id est laborum.
      </Paragraph>
      <Heading>Input</Heading>
      <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
        The first line contains a single integer N (1≤N≤106), denoting the
        number of days Mel will get ingredients. <br />
        The next line contains N integers a1,...,aN (1≤ai≤5), where Mel can
        build layer ai on the ith day, if he chooses.
      </Paragraph>
      <Heading>Output</Heading>
      <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
        Output one integer, the number of cakes Mel can finish.
      </Paragraph>
      <Heading>Example</Heading>
      <div className="exampleContainer">
        <div className="exampleEntryCardWrapper inputWrapper">
          <Card pad="small" gap="small" style={{ padding: "0px" }}>
            <CardHeader background="brandLight">
              <div style={{ margin: "14px 0 14px 14px" }}>Output</div>
            </CardHeader>
            <CardBody>
              <div style={{ margin: "0 0 14px 14px" }}>
                11 <br /> 1 1 2 3 4 4 5 2 3 4 5
              </div>
            </CardBody>
          </Card>
        </div>
        <div className="exampleEntryCardWrapper">
          <Card pad="small" gap="small" style={{ padding: "0px" }}>
            <CardHeader background="brandLight">
              <div style={{ margin: "14px 0 14px 14px" }}>Output</div>
            </CardHeader>
            <CardBody>
              <div style={{ margin: "0 0 14px 14px" }}>Output</div>
            </CardBody>
          </Card>
        </div>
      </div>
    </>
  );
}
