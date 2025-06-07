"use client"

import React, { useEffect } from "react"
import { format } from "date-fns"
import { Calendar as CalendarIcon } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"

interface DatePickerProps {
  availableDates: Date[];
  onSelect: (date: string) => void;
  value?: string;
}

export function DatePicker({
  availableDates,
  onSelect,
  value,
}: DatePickerProps) {
  const [date, setDate] = React.useState<Date | undefined>(
    value ? new Date(value) : undefined
  );

  useEffect(() => {
    if (value) {
      setDate(new Date(value));
    }
  }, [value]);

  const isDateAvailable = (day: Date) => {
    return availableDates.some((d) =>
        d.getFullYear() === day.getFullYear() &&
        d.getMonth() === day.getMonth() &&
        d.getDate() === day.getDate()
    );
  }

  const handleSelect = (selectedDate?: Date) => {
    setDate(selectedDate);
    if (selectedDate && isDateAvailable(selectedDate)) {
      onSelect(selectedDate.toISOString());
    }
  };

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant={"outline"}
          className={cn(
            "w-[280px] justify-start text-left font-normal",
            !date && "text-muted-foreground"
          )}
        >
          <CalendarIcon className="mr-2 h-4 w-4" />
          {date ? format(date, "PPP") : <span>Pick a date</span>}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0">
        <Calendar
          mode="single"
          selected={date}
          onSelect={handleSelect}
          initialFocus
          disabled={(day) => !isDateAvailable(day)}
        />
      </PopoverContent>
    </Popover>
  );
}