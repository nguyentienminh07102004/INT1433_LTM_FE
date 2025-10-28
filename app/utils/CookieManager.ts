import { createCookie } from "react-router";

export const cookieToken = createCookie("token", {
	maxAge: 60 * 60 * 24,
	httpOnly: true,
});
