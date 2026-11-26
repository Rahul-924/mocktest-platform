import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { errMsg } from "../api.js";

export default function Dashboard() {
  const [exams, setExams] = useState([]);
  const [attempts, setAttempts] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([api.get("/api/exams"), api.get("/api/attempts/my")])
      .then(([e, a]) => { setExams(e.data); setAttempts(a.data); })
      .catch((err) => setError(errMsg(err)));
  }, []);

  return (
    <>
      <h2>Available tests</h2>
      {error && <p className="error">{error}</p>}
      {exams.length === 0 && !error && <p className="muted">No tests yet.</p>}
      <div className="grid">
        {exams.map((x) => (
          <div className="card" key={x.id}>
            <h3>{x.title}</h3>
            <p className="muted">{x.description}</p>
            <p>{x.questionCount} questions &middot; {x.durationMinutes} min</p>
            <div className="row">
              <Link className="btn" to={`/exam/${x.id}`}>Start / resume</Link>
              <Link className="btn ghost" to={`/leaderboard/${x.id}`}>Leaderboard</Link>
            </div>
          </div>
        ))}
      </div>

      <h2>My attempts</h2>
      {attempts.length === 0 ? <p className="muted">You have not completed a test yet.</p> : (
        <table>
          <thead><tr><th>Test</th><th>Score</th><th>Date</th><th></th></tr></thead>
          <tbody>
            {attempts.map((a) => (
              <tr key={a.id}>
                <td>{a.examTitle}</td>
                <td>{a.score} / {a.total}</td>
                <td>{new Date(a.submittedAt).toLocaleString()}</td>
                <td><Link to={`/result/${a.id}`}>Review</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
