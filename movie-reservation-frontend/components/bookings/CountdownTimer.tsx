import { useState, useEffect } from 'react';

interface CountdownTimerProps {
  targetDate: string | Date;
  fixedColor: string | null;
}

const CountdownTimer = ({ targetDate, fixedColor }: CountdownTimerProps) => {
  const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

  useEffect(() => {
    const intervalId = setInterval(() => {
      setTimeLeft(calculateTimeLeft());
    }, 1000);

    return () => clearInterval(intervalId);
  }, []);

  function calculateTimeLeft() {
    const now = new Date();
    const difference = new Date(targetDate).getTime() - now.getTime();

    if (difference <= 0) {
      return { minutes: 0, seconds: 0 };
    }

    const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((difference % (1000 * 60)) / 1000);

    return { minutes, seconds };
  }

  let colorClass = "text-white";
  if (timeLeft.minutes === 0 && timeLeft.seconds <= 30) {
    colorClass = "text-red-500";
  } else if (timeLeft.minutes === 0 && timeLeft.seconds <= 59) {
    colorClass = "text-yellow-500";
  }

  return (
    <div className={fixedColor ? fixedColor : colorClass}>
      {`${timeLeft.minutes}m ${timeLeft.seconds}s`}
    </div>
  );
};

export default CountdownTimer;