import "./TaskDescription.css";

import { Card, CardBody, CardHeader, Heading, Paragraph } from "grommet";

import ContestNav from "../../common/ContestNav";
import useLocalStorage from "../../common/useLocalStorage";
import useQuery from "../../common/useQuery";

function getNormalizedExamples(examples) {
  let normExamples = examples.map((el) => {
    return {
      input: String(el[0])
        .split("\n")
        .map(function (item, idx) {
          return (
            <span key={idx}>
              {item}
              <br />
            </span>
          );
        }),
      output: String(el[1])
        .split("\n")
        .map(function (item, idx) {
          return (
            <span key={idx}>
              {item}
              <br />
            </span>
          );
        }),
    };
  });

  return normExamples;
}

export default function TaskDescription() {
  const query = useQuery();
  const [problems] = useLocalStorage("Problems", []);
  const problem = problems.filter((el) => el.name === query.get("name"))[0];
  const examples = getNormalizedExamples(problem.examples);
  // console.log(problem);
  return (
    <>
      <ContestNav />
      <Heading style={{ lineHeight: "20px" }}>{problem.name}</Heading>
      <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
        {problem.description}
      </Paragraph>
      <Heading>Input</Heading>
      <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
        {problem.inputDescription}
      </Paragraph>
      <Heading>Output</Heading>
      <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
        {problem.outputDescription}
      </Paragraph>
      <Heading>Example</Heading>

      <div className="exampleContainer">
        <div className="exampleEntryCardWrapper inputWrapper">
          <Card pad="small" gap="small" style={{ padding: "0px" }}>
            <CardHeader background="brandLight">
              <div style={{ margin: "14px 0 14px 14px" }}>Input</div>
            </CardHeader>
            <CardBody>
              <div style={{ margin: "0 0 14px 14px" }}>{examples[0].input}</div>
            </CardBody>
          </Card>
        </div>

        <div className="exampleEntryCardWrapper">
          <Card pad="small" gap="small" style={{ padding: "0px" }}>
            <CardHeader background="brandLight">
              <div style={{ margin: "14px 0 14px 14px" }}>Output</div>
            </CardHeader>
            <CardBody>
              <div style={{ margin: "0 0 14px 14px" }}>{examples[0].output}</div>
            </CardBody>
          </Card>
        </div>
      </div>
    </>
  );
}
