import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { errMsg } from "../api.js";
import { useAuth } from "../auth.jsx";

export default function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const { data } = await api.post("/api/auth/login", form);
      login(data);
      navigate("/");
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <form className="card narrow" onSubmit={submit}>
      <h2>Log in</h2>
      {error && <p className="error">{error}</p>}
      <label>Email<input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
      <label>Password<input type="password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></label>
      <button className="btn" type="submit">Log in</button>
      <p className="muted">New here? <Link to="/register">Create an account</Link></p>
    </form>
  );
}
