import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api, { errMsg } from "../api.js";

export default function Result() {
  const { attemptId } = useParams();
  const [res, setRes] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get(`/api/attempts/${attemptId}`).then((r) => setRes(r.data)).catch((e) => setError(errMsg(e)));
  }, [attemptId]);

  if (error) return <p className="error">{error}</p>;
  if (!res) return <p className="muted">Loading...</p>;

  return (
    <>
      <h2>{res.examTitle}</h2>
      <div className="card"><h1>{res.score} / {res.total}</h1></div>
      {res.review.map((q, i) => (
        <div className="card" key={q.id}>
          <p><strong>{i + 1}.</strong> {q.text}</p>
          {["A", "B", "C", "D"].map((opt) => {
            const cls = opt === q.correct ? "option right" : opt === q.selected ? "option wrong" : "option";
            return <div className={cls} key={opt}>{opt}. {q[`option${opt}`]}</div>;
          })}
          {!q.selected && <p className="muted">Not answered</p>}
        </div>
      ))}
      <Link className="btn" to="/">Back to dashboard</Link>
    </>
  );
}
