const AUTH_KEY = "gestionaleSession";

function getSession() {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) {
        return null;
    }
    try {
        const parsed = JSON.parse(raw);
        if (parsed && parsed.token && parsed.username) {
            return parsed;
        }
        return null;
    } catch (e) {
        localStorage.removeItem(AUTH_KEY);
        return null;
    }
}

function requireAuth() {
    if (!getSession()) {
        window.location.href = "index.html";
    }
}

function signIn(session) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(session));
}

function currentUser() {
    const session = getSession();
    return session ? session.username : "";
}

function currentRole() {
    const session = getSession();
    return session ? (session.ruolo || "") : "";
}

function authToken() {
    const session = getSession();
    return session ? session.token : "";
}

function signOut() {
    localStorage.removeItem(AUTH_KEY);
    window.location.href = "index.html";
}
