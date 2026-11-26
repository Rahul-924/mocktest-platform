import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api, { errMsg } from "../api.js";

export default function TakeTest() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [answers, setAnswers] = useState({});   // { questionId: "A".."D" }
  const [secondsLeft, setSecondsLeft] = useState(null);
  const [error, setError] = useState("");
  const submitted = useRef(false);               // stops double submission (timer + button)

  // Start (or resume) the attempt. The server owns the deadline, so a refresh cannot reset the timer.
  useEffect(() => {
    api.post(`/api/exams/${id}/start`)
      .then((res) => setData(res.data))
      .catch((err) => setError(errMsg(err)));
  }, [id]);

  const submit = useCallback(async () => {
    if (submitted.current || !data) return;
    submitted.current = true;
    try {
      const res = await api.post(`/api/attempts/${data.attemptId}/submit`, { answers });
      navigate(`/result/${res.data.attemptId}`, { replace: true });
    } catch (err) {
      submitted.current = false;
      setError(errMsg(err));
    }
  }, [data, answers, navigate]);

  // Countdown based on the server's deadline. Auto-submits at zero.
  useEffect(() => {
    if (!data) return;
    const tick = () => {
      const left = Math.max(0, Math.round((new Date(data.deadline) - Date.now()) / 1000));
      setSecondsLeft(left);
      if (left === 0) submit();
    };
    tick();
    const timer = setInterval(tick, 1000);
    return () => clearInterval(timer);
  }, [data, submit]);

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p className="muted">Loading test...</p>;

  const mm = String(Math.floor((secondsLeft ?? 0) / 60)).padStart(2, "0");
  const ss = String((secondsLeft ?? 0) % 60).padStart(2, "0");
  const answered = Object.keys(answers).length;

  return (
    <>
      <div className="sticky">
        <strong>{data.examTitle}</strong>
        <span className={secondsLeft < 60 ? "timer danger" : "timer"}>{mm}:{ss}</span>
      </div>
      {data.questions.map((q, i) => (
        <div className="card" key={q.id}>
          <p><strong>{i + 1}.</strong> {q.text}</p>
          {["A", "B", "C", "D"].map((opt) => (
            <label className="option" key={opt}>
              <input type="radio" name={`q${q.id}`} checked={answers[q.id] === opt}
                     onChange={() => setAnswers({ ...answers, [q.id]: opt })} />
              {opt}. {q[`option${opt}`]}
            </label>
          ))}
        </div>
      ))}
      <button className="btn" onClick={submit}>Submit ({answered}/{data.questions.length} answered)</button>
    </>
  );
}
