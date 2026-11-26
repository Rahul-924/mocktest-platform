import { useEffect, useState } from "react";
import api, { errMsg } from "../api.js";

const emptyQ = { text: "", optionA: "", optionB: "", optionC: "", optionD: "", correctOption: "A" };

export default function Admin() {
  const [exams, setExams] = useState([]);
  const [selected, setSelected] = useState(null);   // full exam including correct answers
  const [examForm, setExamForm] = useState({ title: "", description: "", durationMinutes: 10 });
  const [q, setQ] = useState(emptyQ);
  const [msg, setMsg] = useState("");

  const loadExams = () => api.get("/api/exams").then((r) => setExams(r.data)).catch((e) => setMsg(errMsg(e)));
  useEffect(() => { loadExams(); }, []);

  const open = (id) => api.get(`/api/admin/exams/${id}`).then((r) => setSelected(r.data)).catch((e) => setMsg(errMsg(e)));

  const createExam = async (e) => {
    e.preventDefault();
    setMsg("");
    try {
      const { data } = await api.post("/api/admin/exams", { ...examForm, durationMinutes: Number(examForm.durationMinutes) });
      setExamForm({ title: "", description: "", durationMinutes: 10 });
      await loadExams();
      open(data.id);
    } catch (err) { setMsg(errMsg(err)); }
  };

  const addQuestion = async (e) => {
    e.preventDefault();
    setMsg("");
    try {
      await api.post(`/api/admin/exams/${selected.id}/questions`, q);
      setQ(emptyQ);
      open(selected.id);
      loadExams();
    } catch (err) { setMsg(errMsg(err)); }
  };

  return (
    <>
      <h2>Admin</h2>
      {msg && <p className="error">{msg}</p>}
      <div className="grid">
        <form className="card" onSubmit={createExam}>
          <h3>New test</h3>
          <label>Title<input required maxLength={150} value={examForm.title} onChange={(e) => setExamForm({ ...examForm, title: e.target.value })} /></label>
          <label>Description<input maxLength={500} value={examForm.description} onChange={(e) => setExamForm({ ...examForm, description: e.target.value })} /></label>
          <label>Duration (minutes)<input type="number" min={1} max={300} required value={examForm.durationMinutes} onChange={(e) => setExamForm({ ...examForm, durationMinutes: e.target.value })} /></label>
          <button className="btn" type="submit">Create</button>
        </form>
        <div className="card">
          <h3>Existing tests</h3>
          {exams.map((x) => (<p key={x.id}><button className="link" onClick={() => open(x.id)}>{x.title}</button> ({x.questionCount} questions)</p>))}
        </div>
      </div>

      {selected && (
        <>
          <h3>{selected.title}: {selected.questions.length} questions</h3>
          {selected.questions.map((x, i) => (
            <div className="card" key={x.id}>
              <p><strong>{i + 1}.</strong> {x.text}</p>
              <p className="muted">A) {x.optionA} | B) {x.optionB} | C) {x.optionC} | D) {x.optionD}. Correct: <strong>{x.correctOption}</strong></p>
            </div>
          ))}
          <form className="card" onSubmit={addQuestion}>
            <h3>Add question</h3>
            <label>Question<input required maxLength={1000} value={q.text} onChange={(e) => setQ({ ...q, text: e.target.value })} /></label>
            {["A", "B", "C", "D"].map((o) => (
              <label key={o}>Option {o}<input required maxLength={300} value={q[`option${o}`]} onChange={(e) => setQ({ ...q, [`option${o}`]: e.target.value })} /></label>
            ))}
            <label>Correct answer
              <select value={q.correctOption} onChange={(e) => setQ({ ...q, correctOption: e.target.value })}>
                {["A", "B", "C", "D"].map((o) => <option key={o}>{o}</option>)}
              </select>
            </label>
            <button className="btn" type="submit">Add question</button>
          </form>
        </>
      )}
    </>
  );
}
