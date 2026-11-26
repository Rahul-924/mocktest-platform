import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api, { errMsg } from "../api.js";

export default function Leaderboard() {
  const { id } = useParams();
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get(`/api/exams/${id}/leaderboard`).then((r) => setRows(r.data)).catch((e) => setError(errMsg(e)));
  }, [id]);

  return (
    <>
      <h2>Leaderboard</h2>
      {error && <p className="error">{error}</p>}
      {rows.length === 0 ? <p className="muted">No submissions yet.</p> : (
        <table>
          <thead><tr><th>#</th><th>Name</th><th>Best score</th></tr></thead>
          <tbody>
            {rows.map((r, i) => (<tr key={i}><td>{i + 1}</td><td>{r.name}</td><td>{r.score} / {r.total}</td></tr>))}
          </tbody>
        </table>
      )}
      <Link className="btn" to="/">Back</Link>
    </>
  );
}
