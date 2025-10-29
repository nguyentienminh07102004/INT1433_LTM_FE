import React from "react";
import type { MusicResponse } from "~/types/Music";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import CardActions from "@mui/material/CardActions";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import FormControl from "@mui/material/FormControl";
import RadioGroup from "@mui/material/RadioGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import Radio from "@mui/material/Radio";
import FormHelperText from "@mui/material/FormHelperText";
import Chip from "@mui/material/Chip";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import PauseIcon from "@mui/icons-material/Pause";
import Box from "@mui/material/Box";

export default function AudioComponent({
	music,
	onAnswerSelect,
}: {
	music: MusicResponse;
	onAnswerSelect?: (answerId: number) => void;
}) {
	const { url, answers, title, description } = music;
	const audioRef = React.useRef<HTMLAudioElement | null>(null);
	const [isPlaying, setIsPlaying] = React.useState(false);
	const [selectedAnswerId, setSelectedAnswerId] = React.useState<number | null>(null);
	const [answered, setAnswered] = React.useState(false);

	React.useEffect(() => {
		const audio = audioRef.current;
		if (!audio) return;
		const onEnded = () => setIsPlaying(false);
		audio.addEventListener("ended", onEnded);
		return () => audio.removeEventListener("ended", onEnded);
	}, []);

	const togglePlay = () => {
		const audio = audioRef.current;
		if (!audio) return;
		if (isPlaying) {
			audio.pause();
			setIsPlaying(false);
		} else {
			// play() returns a promise in modern browsers
			void audio.play();
			setIsPlaying(true);
		}
	};

	return (
		<Card>
			<CardContent>
				<Typography variant="h6" component="div">
					{title}
				</Typography>
				{description && (
					<Typography variant="body2" color="text.secondary" paragraph>
						{description}
					</Typography>
				)}

				<Box display="flex" alignItems="center" gap={1}>
					<IconButton aria-label={isPlaying ? "Pause" : "Play"} onClick={togglePlay}>
						{isPlaying ? <PauseIcon /> : <PlayArrowIcon />}
					</IconButton>
					<audio ref={audioRef} controls src={url} style={{ width: "100%" }} />
				</Box>
			</CardContent>

			{answers && answers.length > 0 && (
				<CardActions>
					<Stack spacing={1} width="100%">
						<Typography variant="subtitle2">Answers</Typography>
						<FormControl component="fieldset">
							<RadioGroup
								value={selectedAnswerId !== null ? String(selectedAnswerId) : ""}
								onChange={(e) => {
									const id = parseInt(e.target.value, 10);
									setSelectedAnswerId(id);
									setAnswered(true);
									if (onAnswerSelect) onAnswerSelect(id);
								}}
								aria-label="answers"
								name={`answers-${music.id}`}
							>
								{answers.map((a) => {
									const label = <span>{a.description}</span>;
									return (
										<FormControlLabel
											key={a.id}
											value={String(a.id)}
											control={<Radio disabled={answered} />}
											label={label}
										/>
									);
								})}
							</RadioGroup>
							{answered && (
								<FormHelperText>
									<Chip label="Answer submitted" color="default" size="small" />
								</FormHelperText>
							)}
						</FormControl>
					</Stack>
				</CardActions>
			)}
		</Card>
	);
}
