import { io } from "socket.io-client";

export default function SocketIOConfiguration(token: string) {
	const socket = io(`http://localhost:9092?token=${token}`);
	return socket;
}
