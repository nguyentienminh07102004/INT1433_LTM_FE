import Button from "@mui/material/Button";
import type { Socket } from "socket.io-client";

export default function StartComponent({ socket, musicId }: { socket: Socket | null, musicId: number }) {
	return (
		<>
			<div className="flex justify-center items-center h-full w-full">
				<Button onClick={() => {
                    socket?.emit("topic/changeQuestion", { musicId: musicId.toString() });
                }}>Start</Button>
			</div>
		</>
	);
}
