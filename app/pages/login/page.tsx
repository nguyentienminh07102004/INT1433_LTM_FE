import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import CssBaseline from "@mui/material/CssBaseline";
import FormControl from "@mui/material/FormControl";
import Input from "@mui/material/Input";
import InputLabel from "@mui/material/InputLabel";
import React from "react";
import { createCookie, Form, redirect } from "react-router";
import type { Route } from "./+types/page";

export async function action({ request }: Route.ActionArgs) {
	let formData = await request.formData();
	const username = formData.get("username");
	const password = formData.get("password");
	const res = await fetch("http://localhost:8080/users/login", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify({ username, password }),
	});
	if (res.ok) {
		const data = await res.text();
        const cookie = createCookie("token", {
            maxAge: 60 * 60 * 24,
            httpOnly: true,
        });
        return redirect("/", {
            headers: {
                "Set-Cookie": await cookie.serialize(data),
            },
        });
	}
}

export default function LoginPage() {
	return (
		<>
			<React.Fragment>
				<CssBaseline />
				<div className="bg-white h-screen w-screen">
					<Container maxWidth="xl" className="h-full">
						<Form method="POST" className="flex justify-center items-center flex-col h-full gap-10">
							<FormControl className="w-1/2">
								<InputLabel htmlFor="username">Username: </InputLabel>
								<Input type="text" name="username" id="username" />
							</FormControl>
							<FormControl className="w-1/2">
								<InputLabel htmlFor="password">Password: </InputLabel>
								<Input name="password" id="password" type="password" />
							</FormControl>
							<Button type="submit" color="primary">
								LOGIN
							</Button>
						</Form>
					</Container>
				</div>
			</React.Fragment>
		</>
	);
}
