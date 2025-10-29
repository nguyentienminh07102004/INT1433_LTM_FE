export interface UserResponse {
    id: number;
    username: string;
    fullName: string;
    status: "ONLINE" | "OFFLINE";
}