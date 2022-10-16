import Button from "@mui/material/Button";
import ButtonGroup from "@mui/material/ButtonGroup";
import { useEffect } from "react";

export default function ContestNav({current}) {
  return (
    <ButtonGroup
      variant="outlined"
      aria-label="outlined button group"
      size="large"
      color="warning"
    >
      <Button disabled={current === "tasks"}> Tasks</Button>
      <Button disabled={current === "sumbit"}> Submit solution</Button>
      <Button disabled={current === "submissions"}> My submissions</Button>
    </ButtonGroup>
  );
}
