import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import Badge from "@mui/material/Badge";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Typography from "@mui/material/Typography";
import type { Socket } from "socket.io-client";
import type { UserResponse } from "~/types/User";

export default function UserCardComponent({ user, socket }: { user: UserResponse, socket: Socket | null }) {
	const handleInvite = () => {
		socket?.emit("topic/inviteUser", user.username);
	}
	return (
		<>
			<Card>
				<CardContent className="flex justify-between items-center">
					<Badge variant="dot" color="primary" invisible={user.status !== "ONLINE"}>
						<AccountCircleIcon className="w-7" />
					</Badge>
					<Typography>{user.fullName}</Typography>
					{user.status === "ONLINE" && (
						<Button variant="contained" color="primary" onClick={handleInvite}>
							Invite
						</Button>
					)}
				</CardContent>
			</Card>
		</>
	);
}
