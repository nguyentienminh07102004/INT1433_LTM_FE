import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import Grid from "@mui/material/Grid";
import Stack from "@mui/material/Stack";
import { styled } from "@mui/material/styles";
import Paper from "@mui/material/Paper";

export default function HomePage() {
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
								<Item>Column 1 - Row 1</Item>
								<Item>Column 1 - Row 2</Item>
								<Item>Column 1 - Row 3</Item>
							</Stack>
						</Grid>
						<Grid size={8}>
							<Item sx={{ height: "100%", boxSizing: "border-box" }}>Column 2</Item>
						</Grid>
					</Grid>
				</Box>
			</Container>
            </div>
		</>
	);
}
