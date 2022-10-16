import Button from "@mui/material/Button";
import ButtonGroup from "@mui/material/ButtonGroup";
import { useEffect } from "react"
import { useNavigate } from "react-router-dom"

export default function ContestNav({ current }) {
  const navigate = useNavigate();

  function onClick() {
    navigate("/tasks")
  }

  return (
    <ButtonGroup
      variant="outlined"
      aria-label="outlined button group"
      size="large"
      color="warning"
    >
      <Button disabled={current === "tasks"} onClick={() => onClick()}>
        Tasks
      </Button>
      <Button disabled={current === "sumbit"}> Submit solution</Button>
      <Button disabled={current === "submissions"}> My submissions</Button>
    </ButtonGroup>
  );
}
