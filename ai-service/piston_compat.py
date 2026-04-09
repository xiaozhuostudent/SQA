from __future__ import annotations

import json
import os
import subprocess
import tempfile
import time
from pathlib import Path

from flask import Flask, jsonify, request

app = Flask(__name__)


RUNTIMES = [
    {
        "language": "python",
        "version": "3",
        "aliases": ["py", "python3"],
    },
    {
        "language": "java",
        "version": "23",
        "aliases": ["java"],
    },
    {
        "language": "c",
        "version": "gnu11",
        "aliases": ["c"],
    },
    {
        "language": "cpp",
        "version": "gnu++17",
        "aliases": ["c++", "cpp", "cc"],
    }
]


def _build_response(language: str, version: str, stdout: str, stderr: str, exit_code: int, elapsed: float, compile_stdout: str = "", compile_stderr: str = "", compile_code: int = 0) -> dict:
    return {
        "language": language,
        "version": version,
        "run": {
            "stdout": stdout,
            "stderr": stderr,
            "code": exit_code,
            "signal": None,
            "output": (stdout or "") + (stderr or ""),
            "runtime": elapsed,
        },
        "compile": {
            "stdout": compile_stdout,
            "stderr": compile_stderr,
            "code": compile_code,
            "signal": None,
            "output": (compile_stdout or "") + (compile_stderr or ""),
        },
    }


def _normalize_language(language: str) -> str:
    normalized = (language or "").lower().strip()
    if normalized in ("py", "python3"):
        return "python"
    if normalized in ("c++", "cc"):
        return "cpp"
    return normalized


def _run_python(code: str, stdin_text: str) -> dict:
    with tempfile.TemporaryDirectory(prefix="piston_compat_py_") as tmp_dir:
        code_path = Path(tmp_dir) / "main.py"
        code_path.write_text(code, encoding="utf-8")
        start = time.perf_counter()
        try:
            proc = subprocess.run(
                ["python3", str(code_path)],
                input=stdin_text,
                text=True,
                capture_output=True,
                timeout=5,
            )
            elapsed = time.perf_counter() - start
            return _build_response("python", "3", proc.stdout, proc.stderr, proc.returncode, round(elapsed, 6))
        except subprocess.TimeoutExpired:
            elapsed = time.perf_counter() - start
            return _build_response("python", "3", "", "Time limit exceeded", 124, round(elapsed, 6))
        except Exception as exc:  # pragma: no cover
            elapsed = time.perf_counter() - start
            return _build_response("python", "3", "", f"execution error: {exc}", 1, round(elapsed, 6))


def _run_java(code: str, stdin_text: str) -> dict:
    with tempfile.TemporaryDirectory(prefix="piston_compat_java_") as tmp_dir:
        workdir = Path(tmp_dir)
        source_path = workdir / "Main.java"
        source_path.write_text(code, encoding="utf-8")
        compile_start = time.perf_counter()
        compile_proc = subprocess.run(
            ["javac", "Main.java"],
            cwd=workdir,
            text=True,
            capture_output=True,
            timeout=20,
        )
        compile_elapsed = time.perf_counter() - compile_start
        if compile_proc.returncode != 0:
            return _build_response(
                "java",
                "23",
                "",
                compile_proc.stderr,
                compile_proc.returncode,
                round(compile_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )

        run_start = time.perf_counter()
        try:
            run_proc = subprocess.run(
                ["java", "Main"],
                cwd=workdir,
                input=stdin_text,
                text=True,
                capture_output=True,
                timeout=5,
            )
            run_elapsed = time.perf_counter() - run_start
            return _build_response(
                "java",
                "23",
                run_proc.stdout,
                run_proc.stderr,
                run_proc.returncode,
                round(run_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )
        except subprocess.TimeoutExpired:
            run_elapsed = time.perf_counter() - run_start
            return _build_response(
                "java",
                "23",
                "",
                "Time limit exceeded",
                124,
                round(run_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )
        except Exception as exc:  # pragma: no cover
            run_elapsed = time.perf_counter() - run_start
            return _build_response(
                "java",
                "23",
                "",
                f"execution error: {exc}",
                1,
                round(run_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )


def _run_c_or_cpp(code: str, stdin_text: str, language: str) -> dict:
    extension = ".c" if language == "c" else ".cpp"
    compiler = "gcc" if language == "c" else "g++"
    version = "gnu11" if language == "c" else "gnu++17"
    with tempfile.TemporaryDirectory(prefix=f"piston_compat_{language}_") as tmp_dir:
        workdir = Path(tmp_dir)
        source_path = workdir / f"main{extension}"
        binary_path = workdir / "main.out"
        source_path.write_text(code, encoding="utf-8")
        compile_start = time.perf_counter()
        compile_proc = subprocess.run(
            [compiler, str(source_path), "-O2", "-std=" + version, "-o", str(binary_path)],
            text=True,
            capture_output=True,
            timeout=20,
        )
        compile_elapsed = time.perf_counter() - compile_start
        if compile_proc.returncode != 0:
            return _build_response(
                language,
                version,
                "",
                compile_proc.stderr,
                compile_proc.returncode,
                round(compile_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )

        run_start = time.perf_counter()
        try:
            run_proc = subprocess.run(
                [str(binary_path)],
                cwd=workdir,
                input=stdin_text,
                text=True,
                capture_output=True,
                timeout=5,
            )
            run_elapsed = time.perf_counter() - run_start
            return _build_response(
                language,
                version,
                run_proc.stdout,
                run_proc.stderr,
                run_proc.returncode,
                round(run_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )
        except subprocess.TimeoutExpired:
            run_elapsed = time.perf_counter() - run_start
            return _build_response(
                language,
                version,
                "",
                "Time limit exceeded",
                124,
                round(run_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )
        except Exception as exc:  # pragma: no cover
            run_elapsed = time.perf_counter() - run_start
            return _build_response(
                language,
                version,
                "",
                f"execution error: {exc}",
                1,
                round(run_elapsed, 6),
                compile_stdout=compile_proc.stdout,
                compile_stderr=compile_proc.stderr,
                compile_code=compile_proc.returncode,
            )


@app.get("/api/v2/piston/runtimes")
def runtimes():
    return jsonify(RUNTIMES)


@app.post("/api/v2/piston/execute")
def execute():
    payload = request.get_json(silent=True) or {}
    language = _normalize_language(str(payload.get("language", "")))
    stdin_text = payload.get("stdin", "") or ""
    files = payload.get("files") or []

    if language not in ("python", "java", "c", "cpp"):
        return jsonify(_build_response(language or "unknown", "", "", f"unsupported language: {language}", 1, 0.0))

    if not files or not isinstance(files, list):
        return jsonify(_build_response(language, "", "", "files is required", 1, 0.0))

    code = str(files[0].get("content", ""))
    if not code.strip():
        return jsonify(_build_response(language, "", "", "empty code", 1, 0.0))

    if language == "python":
        return jsonify(_run_python(code, stdin_text))
    if language == "java":
        return jsonify(_run_java(code, stdin_text))
    return jsonify(_run_c_or_cpp(code, stdin_text, language))


@app.get("/health")
def health():
    return jsonify({"status": "ok", "service": "piston_compat"})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5053)
