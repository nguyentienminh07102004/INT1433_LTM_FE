import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import { styled } from "@mui/material/styles";
import React from "react";
import { redirect } from "react-router";
import type { Socket } from "socket.io-client";
import UserCardComponent from "~/components/UserCardComponent";
import SocketIOConfiguration from "~/configuration/SocketIOConfiguration";
import type { UserResponse } from "~/types/User";
import { cookieToken } from "~/utils/CookieManager";
import type { Route } from "./+types/page";

export async function loader({ request }: Route.LoaderArgs) {
	const token = await cookieToken.parse(request.headers.get("Cookie") || "");
	if (!token) {
		return redirect("/login");
	}
	return token;
}

export default function HomePage({ loaderData }: Route.ComponentProps) {
	const socket = React.useRef<Socket>(SocketIOConfiguration(loaderData));
	const [users, setUsers] = React.useState<UserResponse[]>([]);
	React.useEffect(() => {
		const onConnect = () => {
			console.log("Connected to server with id:", socket.current.id);
		};
		const getAllUsers = (data: UserResponse[]) => {
			setUsers(data);
		};
		const changeUserStatus = ({ username, status }: { username: string; status: "ONLINE" | "OFFLINE" }) => {
			setUsers((prevUsers) => prevUsers.map((user) => (user.username === username ? { ...user, status: status } : user)));
		};
		socket.current.on("topic/getAllUsersResponse", getAllUsers);
		socket.current.on("topic/changeStatus", changeUserStatus);
		socket.current.on("connect", onConnect);
		return () => {
			socket.current.off("connect", onConnect);
			socket.current.off("topic/getAllUsersResponse", getAllUsers);
			socket.current.off("topic/changeStatus", changeUserStatus);
		};
	}, []);
	console.log("Socket ID:", socket.current.id);
	const Item = styled(Paper)(({ theme }) => ({
		backgroundColor: "#fff",
		...theme.typography.body2,
		padding: theme.spacing(1),
		textAlign: "center",
		color: (theme.vars ?? theme).palette.text.secondary,
		...theme.applyStyles("dark", {
			backgroundColor: "#1A2027",
		}),
	}));
	return (
		<>
			<div className="h-screen w-screen">
				<Container className="h-screen w-screen" maxWidth="xl">
					<Box sx={{ flexGrow: 1, height: "100vh", py: 4 }}>
						<Grid container spacing={2} height={"100%"}>
							<Grid size={4}>
								<Stack spacing={2}>
									{users.map((user) => (
										<UserCardComponent key={user.id} user={user} />
									))}
								</Stack>
							</Grid>
							<Grid size={8}></Grid>
						</Grid>
					</Box>
				</Container>
			</div>
		</>
	);
}
