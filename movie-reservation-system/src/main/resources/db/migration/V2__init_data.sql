insert into tb_user ("id", user_name, email, password_hash, user_role, created_at, updated_at) values
(1, 'CBum', 'cbum@gmail.com', 'mJpBfs.DIbN0BsQbpQG3Q31kRHZthxm45N.iZy', 'ADMIN', current_timestamp, current_timestamp);

insert into tb_theater ("id", theater_name, full_address_line, created_at, updated_at) values
(1, 'Cinema Plus', 'Av. das Nações Unidas, 12901 - Brooklin, São Paulo - SP, 04578-910', current_timestamp, current_timestamp);

insert into tb_screen ("id", screen_name, theater_fk, created_at, updated_at) values
(1, 'Standard 1', 1, current_timestamp, current_timestamp),
(2, 'Special 1', 1, current_timestamp, current_timestamp);

insert into tb_seat ("id", seat_row, seat_number, seat_type, screen_fk, created_at, updated_at) values
(1, 'A', 1, 'STANDARD', 1, current_timestamp, current_timestamp),
(2, 'A', 2, 'STANDARD', 1, current_timestamp, current_timestamp),
(3, 'A', 3, 'STANDARD', 1, current_timestamp, current_timestamp),
(4, 'A', 4, 'PWD', 1, current_timestamp, current_timestamp),
(5, 'A', 5, 'VIP', 1, current_timestamp, current_timestamp),
(6, 'B', 1, 'STANDARD', 1, current_timestamp, current_timestamp),
(7, 'B', 2, 'STANDARD', 1, current_timestamp, current_timestamp),
(8, 'B', 3, 'STANDARD', 1, current_timestamp, current_timestamp),
(9, 'B', 4, 'PWD', 1, current_timestamp, current_timestamp),
(10, 'B', 5, 'VIP', 1, current_timestamp, current_timestamp),
(11, 'A', 1, 'STANDARD', 2, current_timestamp, current_timestamp),
(12, 'A', 2, 'STANDARD', 2, current_timestamp, current_timestamp),
(13, 'A', 3, 'STANDARD', 2, current_timestamp, current_timestamp),
(14, 'A', 4, 'PWD', 2, current_timestamp, current_timestamp),
(15, 'A', 5, 'VIP', 2, current_timestamp, current_timestamp),
(16, 'B', 1, 'STANDARD', 2, current_timestamp, current_timestamp),
(17, 'B', 2, 'STANDARD', 2, current_timestamp, current_timestamp),
(18, 'B', 3, 'STANDARD', 2, current_timestamp, current_timestamp),
(19, 'B', 4, 'PWD', 2, current_timestamp, current_timestamp),
(20, 'B', 5, 'VIP', 2, current_timestamp, current_timestamp);

insert into tb_movie ("id", title, description, genre, minutes_duration, release_date, created_at, updated_at) values
(1, 'The Last Horizon', 'A crew of astronauts ventures beyond the solar system to find a new home for humanity.', 'SCI_FI', 138, '2024-11-10', current_timestamp, current_timestamp),
(2, 'Silent Echoes', 'A mysterious soundwave causes strange behavior in a small town, leading to a deeper government secret.', 'THRILLER', 112, '2024-07-22', current_timestamp, current_timestamp),
(3, 'Canvas of Dreams', 'An aspiring painter struggles with self-doubt while trying to win a national art competition.', 'DRAMA', 124, '2025-01-14', current_timestamp, current_timestamp),
(4, 'Laugh Lines', 'A washed-up comedian gets a second chance at fame when he mentors a rising TikTok star.', 'COMEDY', 96, '2024-09-03', current_timestamp, current_timestamp),
(5, 'Phantom of the Forest', 'A horror tale about a cursed woodland spirit haunting a group of campers.', 'HORROR', 105, '2025-03-31', current_timestamp, current_timestamp),
(6, 'Echoes of the Abyss', 'A deep-sea diver unravels the secrets of a lost civilization at the ocean floor.', 'SCI_FI', 142, '2024-08-14', current_timestamp, current_timestamp),
(7, 'The Forgotten Kingdom', 'An adventurer stumbles upon an ancient kingdom locked in time.', 'FANTASY', 120, '2024-12-19', current_timestamp, current_timestamp),
(8, 'Winds of Fury', 'A storm-chaser battles his past while following a series of catastrophic tornadoes.', 'ACTION', 128, '2025-05-09', current_timestamp, current_timestamp),
(9, 'Through the Veil', 'Paranormal detectives investigate recurring ghost sightings in a haunted mansion.', 'HORROR', 117, '2025-10-13', current_timestamp, current_timestamp),
(10, 'Code of Steel', 'An AI robot discovers emotions and struggles against its creators.', 'SCI_FI', 110, '2024-06-20', current_timestamp, current_timestamp),
(11, 'Hearts in Midnight', 'Two strangers fall in love when they repeatedly meet at a 24-hour diner.', 'ROMANCE', 95, '2025-02-14', current_timestamp, current_timestamp),
(12, 'The Outlander', 'An alien exiled on Earth must save the planet from invaders.', 'SCI_FI', 130, '2024-10-05', current_timestamp, current_timestamp),
(13, 'Game Breakers', 'A national esports team struggles to prove themselves in a global championship.', 'SPORTS', 110, '2025-04-21', current_timestamp, current_timestamp),
(14, 'The Hidden Portrait', 'A journalist investigates the mysterious disappearance of a world-famous artist.', 'MYSTERY', 116, '2025-08-11', current_timestamp, current_timestamp),
(15, 'Dust and Gold', 'A gritty tale of survival and revenge in the Wild West.', 'WESTERN', 140, '2024-07-29', current_timestamp, current_timestamp),
(16, 'Pacific Blaze', 'A firefighter braves the flames of California wildfires to save his community.', 'DRAMA', 105, '2024-11-22', current_timestamp, current_timestamp),
(17, 'Time Paradox', 'A scientist accidentally alters history and must fix the timeline.', 'SCI_FI', 118, '2025-03-14', current_timestamp, current_timestamp),
(18, 'The Final Betrayal', 'A spy uncovers a conspiracy while being framed for treason.', 'THRILLER', 126, '2025-07-18', current_timestamp, current_timestamp),
(19, 'Legends of the Arena', 'Gladiators fight for glory and freedom in ancient Rome.', 'HISTORICAL', 148, '2024-09-02', current_timestamp, current_timestamp),
(20, 'The Prism Keeper', 'A young girl discovers she can control light to fight evil forces.', 'FANTASY', 102, '2025-06-25', current_timestamp, current_timestamp);

insert into tb_session ("id", screen_fk, movie_fk, start_time, end_time, standard_seat_price, vip_seat_price, pwd_seat_price, has_subtitles, audio_language, is_threed, created_at, updated_at) values
(1, 1, 1, '2025-04-23 18:40:00', '2025-06-23 20:58:00', 50.0, 70.0, 25.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(2, 2, 1, '2025-04-25 12:00:00', '2025-05-25 13:30:00', 50.0, 70.0, 25.0, false, 'PORTUGUESE', false, current_timestamp, current_timestamp),
(3, 1, 2, '2025-06-23 21:10:00', '2025-06-23 22:52:00', 45.0, 65.0, 20.0, true, 'ENGLISH', false, current_timestamp, current_timestamp),
(4, 2, 1, '2025-06-24 14:10:00', '2025-06-24 16:14:00', 55.0, 75.0, 30.0, false, 'PORTUGUESE', true, current_timestamp, current_timestamp),
(5, 1, 4, '2025-06-24 17:00:00', '2025-06-24 18:36:00', 50.0, 70.0, 25.0, true, 'ENGLISH', true, current_timestamp, current_timestamp),
(6, 2, 6, '2025-06-24 19:30:00', '2025-06-24 21:52:00', 50.0, 75.0, 25.0, false, 'SPANISH', false, current_timestamp, current_timestamp),
(7, 1, 7, '2025-06-25 16:10:00', '2025-06-25 18:10:00', 48.0, 68.0, 23.0, true, 'ENGLISH', false, current_timestamp, current_timestamp),
(8, 2, 8, '2025-06-25 20:00:00', '2025-06-25 22:08:00', 50.0, 72.0, 27.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(9, 1, 9, '2025-06-26 12:00:00', '2025-06-26 13:57:00', 42.0, 62.0, 20.0, true, 'PORTUGUESE', false, current_timestamp, current_timestamp),
(10, 2, 10, '2025-06-26 18:15:00', '2025-06-26 20:05:00', 50.0, 70.0, 25.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(11, 1, 1, '2025-06-27 13:00:00', '2025-06-27 14:35:00', 40.0, 60.0, 20.0, true, 'FRENCH', false, current_timestamp, current_timestamp),
(12, 2, 12, '2025-06-27 19:30:00', '2025-06-27 21:40:00', 50.0, 70.0, 25.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(13, 1, 13, '2025-06-28 11:30:00', '2025-06-28 13:20:00', 45.0, 65.0, 22.0, true, 'PORTUGUESE', false, current_timestamp, current_timestamp),
(14, 2, 1, '2025-06-28 15:30:00', '2025-06-28 17:26:00', 52.0, 72.0, 28.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(15, 1, 1, '2025-06-29 10:00:00', '2025-06-29 12:20:00', 50.0, 75.0, 25.0, true, 'ENGLISH', true, current_timestamp, current_timestamp),
(16, 2, 16, '2025-06-29 19:00:00', '2025-06-29 20:45:00', 48.0, 70.0, 23.0, false, 'SPANISH', false, current_timestamp, current_timestamp),
(17, 1, 1, '2025-06-30 10:40:00', '2025-06-30 12:38:00', 45.0, 65.0, 22.0, true, 'ENGLISH', true, current_timestamp, current_timestamp),
(18, 2, 18, '2025-06-30 18:20:00', '2025-06-30 20:26:00', 55.0, 75.0, 30.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(19, 1, 1, '2025-07-01 14:00:00', '2025-07-01 16:28:00', 50.0, 70.0, 25.0, true, 'ITALIAN', false, current_timestamp, current_timestamp),
(20, 2, 20, '2025-07-01 19:50:00', '2025-07-01 21:32:00', 54.0, 74.0, 28.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(21, 1, 1, '2025-07-02 12:30:00', '2025-07-02 14:48:00', 50.0, 70.0, 25.0, true, 'PORTUGUESE', false, current_timestamp, current_timestamp),
(22, 2, 5, '2025-07-02 20:10:00', '2025-07-02 21:40:00', 50.0, 72.0, 27.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(23, 1, 6, '2025-07-03 16:30:00', '2025-07-03 18:52:00', 48.0, 68.0, 23.0, false, 'SPANISH', true, current_timestamp, current_timestamp),
(24, 2, 7, '2025-07-03 20:50:00', '2025-07-03 22:50:00', 50.0, 75.0, 25.0, true, 'ENGLISH', false, current_timestamp, current_timestamp),
(25, 1, 8, '2025-07-04 13:00:00', '2025-07-04 15:08:00', 50.0, 72.0, 27.0, true, 'PORTUGUESE', true, current_timestamp, current_timestamp),
(26, 2, 9, '2025-07-04 16:30:00', '2025-07-04 18:30:00', 45.0, 65.0, 20.0, false, 'ENGLISH', true, current_timestamp, current_timestamp),
(27, 1, 1, '2025-07-05 18:20:00', '2025-07-05 20:10:00', 55.0, 75.0, 30.0, true, 'ENGLISH', false, current_timestamp, current_timestamp),
(28, 2, 11, '2025-07-05 20:50:00', '2025-07-05 22:25:00', 50.0, 72.0, 25.0, false, 'FRENCH', true, current_timestamp, current_timestamp),
(29, 1, 12, '2025-07-06 14:30:00', '2025-07-06 16:40:00', 50.0, 70.0, 25.0, true, 'ENGLISH', true, current_timestamp, current_timestamp),
(30, 2, 14, '2025-07-06 17:15:00', '2025-07-06 19:11:00', 54.0, 74.0, 28.0, false, 'SPANISH', false, current_timestamp, current_timestamp);