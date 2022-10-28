import CodeEditor from "@uiw/react-textarea-code-editor";
import { Box, Button, Layer } from "grommet";
import { Close } from "grommet-icons";

export default function SubmissionModal({ show, code, onClose, language }) {
  language ??= "python";
  return (
    <>
      {show && (
        <Layer onEsc={() => onClose()} onClickOutside={() => onClose()}>
          <Box pad="medium" animation={{ type: "fadeIn", duration: 700 }}>
            <Button icon={<Close />} onClick={onClose} />
            <div data-color-mode="light" style={{ padding: "10px 0 10px 0" }}>
              <CodeEditor
                value={code}
                language={language}
                placeholder=""
                padding={15}
                style={{
                  fontSize: 20,
                  fontFamily:
                    "ui-monospace,SFMono-Regular,SF Mono,Consolas,Liberation Mono,Menlo,monospace",
                  background: "#F3F3F3",
                  minHeight: "150px",
                  minWidth: "700px",
                }}
              />
            </div>
          </Box>
        </Layer>
      )}
    </>
  );
}
