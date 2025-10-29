import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import type { UserResponse } from "~/types/User";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import Badge from "@mui/material/Badge";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";

export default function UserCardComponent({ user }: { user: UserResponse }) {
	return (
		<>
			<Card>
				<CardContent className="flex justify-between items-center">
					<Badge variant="dot" color="primary" invisible={user.status !== "ONLINE"}>
						<AccountCircleIcon className="w-7" />
					</Badge>
					<Typography>{user.fullName}</Typography>
					<Button variant="contained" color="primary">
						Invite
					</Button>
				</CardContent>
			</Card>
		</>
	);
}
