// Chat‑style React UI for Chat‑with‑Your‑PDF (inline styles, avatars, timestamps, robust object‑safe rendering)

import React, { useState, useEffect, useRef } from "react";
import axios from "axios";

const API_BASE = "https://pdf-chatbot-api-8kn0.onrender.com"; // backend base URL
// const API_BASE = "http://localhost/8080";

const App = () => {
  const [file, setFile] = useState(null);
  const [question, setQuestion] = useState("");
  const [chatLog, setChatLog] = useState([]);
  const [loading, setLoading] = useState(false);
  const chatEndRef = useRef(null);
  const [docId, setDocId] = useState(null);

  /* file upload */
  const handleFileChange = (e) => setFile(e.target.files[0]);

  const handleUpload = async () => {
    if (!file) return alert("Please select a PDF file first.");
    const formData = new FormData();
    formData.append("file", file);

    try {
      const { data: returnedId } = await axios.post(`${API_BASE}/api/pdf-chat/upload`, formData);
      setDocId(returnedId);
      alert("PDF uploaded successfully.");
    } catch (error) {
      alert(error?.response?.data || "Failed to upload PDF.");
    }
  };

  /* ask question */
  const handleAsk = async () => {
    if (!question.trim()) return;
    setLoading(true);

    const userMsg = {
      role: "user",
      content: question,
      time: new Date().toLocaleTimeString(),
    };
    setChatLog((prev) => [...prev, userMsg]);

    try {
      if (!docId) return alert("Upload a PDF first!");
      const { data } = await axios.post(`${API_BASE}/api/pdf-chat/chat`, {
        question,
        documentId: docId,
      });

      const answerText =
        typeof data === "string" ? data : data?.message || JSON.stringify(data);
      const botMsg = {
        role: "bot",
        content: answerText,
        time: new Date().toLocaleTimeString(),
      };
      setChatLog((prev) => [...prev, botMsg]);
      setQuestion("");
    } catch (err) {
      const payload = err?.response?.data;
      const msg =
        typeof payload === "string"
          ? payload
          : payload?.message || "⚠️ Error processing your request.";
      const botErr = {
        role: "bot",
        content: msg,
        time: new Date().toLocaleTimeString(),
      };
      setChatLog((prev) => [...prev, botErr]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleAsk();
    }
  };

  /* auto‑scroll */
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatLog]);

  const getAvatar = (role) => (role === "user" ? "🧑" : "🤖");

  /* typing dots style */
  const dotStyle = {
    animation: "blink 1.2s infinite",
    fontSize: 18,
    margin: "0 1px",
  };

  return (
    <>
      {/* main wrapper */}
      <div
        style={{
          padding: 20,
          fontFamily: "Arial",
          maxWidth: 600,
          margin: "0 auto",
        }}
      >
        {/* keyframes */}
        <style>{`
        @keyframes blink { 0%{opacity:.2;} 20%{opacity:1;} 100%{opacity:.2;} }
      `}</style>

        <h2>Chat with Your PDF</h2>

        {/* uploader */}
        <div style={{ marginBottom: 10 }}>
          <input type="file" accept="application/pdf" onChange={handleFileChange} />
          <button onClick={handleUpload} style={{ marginLeft: 10 }}>
            Upload
          </button>
        </div>

        {/* chat window */}
        <div
          style={{
            border: "1px solid #ccc",
            padding: 10,
            height: 300,
            overflowY: "auto",
            marginBottom: 10,
          }}
        >
          {chatLog.map((msg, idx) => (
            <div
              key={idx}
              style={{
                marginBottom: 10,
                display: "flex",
                justifyContent: msg.role === "user" ? "flex-end" : "flex-start",
              }}
            >
              <div style={{ display: "flex", alignItems: "flex-end", maxWidth: "80%" }}>
                {msg.role === "bot" && <div style={{ marginRight: 8 }}>{getAvatar("bot")}</div>}
                <div
                  style={{
                    background: msg.role === "user" ? "#daf1fc" : "#f0f0f0",
                    padding: 10,
                    borderRadius: 10,
                  }}
                >
                  <div style={{ fontSize: 14 }}>{msg.content}</div>
                  <div style={{ fontSize: 10, color: "#555", marginTop: 4 }}>{msg.time}</div>
                </div>
                {msg.role === "user" && <div style={{ marginLeft: 8 }}>{getAvatar("user")}</div>}
              </div>
            </div>
          ))}

          {loading && (
            <div style={{ display: "flex", justifyContent: "flex-start", marginBottom: 10 }}>
              <div style={{ display: "flex", alignItems: "flex-end", maxWidth: "80%" }}>
                <div style={{ marginRight: 8 }}>{getAvatar("bot")}</div>
                <div
                  style={{
                    background: "#f0f0f0",
                    padding: 10,
                    borderRadius: 10,
                    fontStyle: "italic",
                    fontSize: 14,
                    display: "flex",
                  }}
                >
                  <span style={{ ...dotStyle }}>•</span>
                  <span style={{ ...dotStyle, animationDelay: "0.2s" }}>•</span>
                  <span style={{ ...dotStyle, animationDelay: "0.4s" }}>•</span>
                </div>
              </div>
            </div>
          )}
          <div ref={chatEndRef} />
        </div>

        {/* input */}
        <div style={{ display: "flex", flexDirection: "column" }}>
          <textarea
            rows={3}
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask a question..."
            style={{ padding: 8, resize: "none" }}
          />
          <button onClick={handleAsk} disabled={loading} style={{ marginTop: 10 }}>
            {loading ? "Thinking..." : "Send"}
          </button>
        </div>
      </div>

      {/* footer signature */}
      <div
        style={{
          position: "fixed",
          bottom: 8,
          right: 12,
          fontSize: 12,
          color: "#666",
          fontFamily: "Arial",
        }}
      >
        Created&nbsp;by&nbsp;Sany&nbsp;Rawat
      </div>
    </>
  );
};

export default App;
