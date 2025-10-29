import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import { styled } from "@mui/material/styles";
import { useSnackbar } from "notistack";
import React from "react";
import { redirect } from "react-router";
import type { Socket } from "socket.io-client";
import UserCardComponent from "~/components/UserCardComponent";
import SocketIOConfiguration from "~/configuration/SocketIOConfiguration";
import type { UserResponse } from "~/types/User";
import { cookieToken } from "~/utils/CookieManager";
import type { Route } from "./+types/page";
import AudioComponent from "~/components/AudioComponent";
import type { MusicResponse } from "~/types/Music";
import StartComponent from "~/components/StartComponent";

export async function loader({ request }: Route.LoaderArgs) {
	const token = await cookieToken.parse(request.headers.get("Cookie") || "");
	if (!token) {
		return redirect("/login");
	}
	return token;
}

export default function HomePage({ loaderData }: Route.ComponentProps) {
	const socket = React.useRef<Socket | null>(null);
	const [users, setUsers] = React.useState<UserResponse[]>([]);
	const { enqueueSnackbar, closeSnackbar } = useSnackbar();
	const [gameIds, setGameIds] = React.useState<number[]>([]);
	const [questionNumber, setQuestionNumber] = React.useState<number>(0);
	const [music, setMusic] = React.useState<MusicResponse | null>(null);

	React.useEffect(() => {
		socket.current = SocketIOConfiguration(loaderData);
		const onConnect = () => {
			console.log("Connected to server with id:", socket.current?.id);
		};
		const getAllUsers = (data: UserResponse[]) => {
			setUsers(data);
		};
		const changeUserStatus = ({ username, status }: { username: string; status: "ONLINE" | "OFFLINE" }) => {
			setUsers((prevUsers) => prevUsers.map((user) => (user.username === username ? { ...user, status: status } : user)));
		};
		const initGame = ({ gameId }: { gameId: number[] }) => {
			setGameIds(gameId);
		};
		const changeQuestion = (data: MusicResponse) => {
			console.log("Received music data:", data);
			setMusic(data);
		};
		const receivedInvite = (data: { from: string }) => {
			enqueueSnackbar(`You have received an invite from ${data.from}`, {
				variant: "default",
				action(key) {
					return (
						<>
							<Button
								color="primary"
								size="small"
								onClick={() => {
									socket.current?.emit("topic/acceptInvite", data.from);
								}}
							>
								Accept
							</Button>
							<Button color="secondary" size="small" onClick={() => closeSnackbar(key)}>
								Dismiss
							</Button>
						</>
					);
				},
			});
		};
		socket.current?.on("topic/getAllUsersResponse", getAllUsers);
		socket.current?.on("topic/changeStatus", changeUserStatus);
		socket.current?.on("connect", onConnect);
		socket.current?.on(`topic/inviteUser`, receivedInvite);
		socket.current?.on("topic/initGame", initGame);
		socket.current?.on("topic/changeQuestion", changeQuestion);
		return () => {
			socket.current?.off("connect", onConnect);
			socket.current?.off("topic/getAllUsersResponse", getAllUsers);
			socket.current?.off("topic/changeStatus", changeUserStatus);
			socket.current?.off(`topic/inviteUser`, receivedInvite);
			socket.current?.off("topic/initGame", initGame);
			socket.current?.off("topic/changeQuestion", changeQuestion);
			socket.current?.disconnect();
			socket.current = null;
		};
	}, [loaderData]);
	console.log("Socket ID:", socket.current?.id);
	return (
		<>
			<div className="h-screen w-screen">
				<Container className="h-screen w-screen" maxWidth="xl">
					<Box sx={{ flexGrow: 1, height: "100vh", py: 4 }}>
						<Grid container spacing={2} height={"100%"} gap={2}>
							<Grid size={4}>
								<Stack spacing={2}>
									{users.map((user) => (
										<UserCardComponent socket={socket.current} key={user.id} user={user} />
									))}
								</Stack>
							</Grid>
							<Grid size={8}>
								{gameIds && gameIds.length > 0 && !music ? (
									<StartComponent socket={socket.current} musicId={gameIds[0]} />
								) : music ? (
									<Paper elevation={3} sx={{ height: "100%" }}>
										<AudioComponent music={music} />
									</Paper>
								) : (
									<></>
								)}
							</Grid>
						</Grid>
					</Box>
				</Container>
			</div>
		</>
	);
}
