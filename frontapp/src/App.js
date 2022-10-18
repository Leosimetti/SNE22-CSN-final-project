import { Grommet, Header, Heading, Page, PageContent } from "grommet";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Home from "./pages/Home/Home";
import TaskDescription from "./pages/TaskDescription/TaskDescription";
import Tasks from "./pages/Tasks/Tasks";
import URLs from "./common/URLs";
import TaskSubmit from "./pages/TaskSubmit/TaskSubmit";

const theme = {
  global: {
    colors: {
      white: "#FFFFFF",
      brand: "#FE8E4E",
      brandLight: "#FFD025",
      blackBorder: "#003c58",
    },
    font: {
      family: "Roboto",
      size: "20px",
      height: "24px",
    },
  },
};

function App() {
  return (
    <Grommet theme={theme}>
      <Router>
        <Page fill background="blackBorder">
          <PageContent background="white" style={{ padding: "0" }}>
            <div style={{ minHeight: "100vh" }}>
              <Header background={"brand"}>
                <Link to="/">
                  <Heading margin="20px" color="white">
                    DVBAJSfPC
                  </Heading>
                </Link>
              </Header>
              <div style={{ margin: "20px" }}>
                <Routes>
                  <Route
                    path={URLs.taskDescription}
                    element={<TaskDescription />}
                  />
                  <Route path={URLs.tasks} element={<Tasks />} />
                  <Route path={URLs.home} element={<Home />} />
                  <Route path={URLs.taskSubmit} element={<TaskSubmit />} />
                </Routes>
              </div>
            </div>
          </PageContent>
        </Page>
      </Router>
    </Grommet>
  );
}

export default App;
