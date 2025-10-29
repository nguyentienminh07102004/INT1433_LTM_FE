export interface AnswerResponse {
  id: number;
  description: string;
}

export interface MusicResponse {
  id: number;
  title: string;
  description: string;
  url: string;
  answers: AnswerResponse[];
}