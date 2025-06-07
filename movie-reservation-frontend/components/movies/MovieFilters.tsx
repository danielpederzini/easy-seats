"use client";

import React, { use, useEffect, useRef, useState } from "react";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { MovieGenre } from "@/lib/types";

interface MovieFiltersProps {
  onFilterChange: (filters: {
    search: string;
    genre: string;
  }) => void;
  filters: {
    search: string;
    genre: string;
  };
}

const MovieFilters: React.FC<MovieFiltersProps> = ({ 
  onFilterChange, 
  filters 
}) => {
  const { search, genre } = filters;
  const debounceRef = useRef<NodeJS.Timeout | null>(null);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newSearch = e.target.value;
    
    // Immediately update UI
    onFilterChange({ 
      search: newSearch, 
      genre 
    });
  };

  const handleGenreChange = (value: string) => {
    onFilterChange({ 
      search, 
      genre: value 
    });
  };

  const handleReset = () => {
    onFilterChange({ 
      search: "", 
      genre: "all" 
    });
  };

  // Cleanup debounce timer
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  return (
    <div className="bg-muted/40 p-4 rounded-lg mb-8">
      <div className="flex flex-col space-y-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search movies..."
              className="pl-10"
              value={search}
              onChange={handleSearchChange}
            />
          </div>
          <Select value={genre} onValueChange={handleGenreChange}>
            <SelectTrigger className="w-full md:w-[180px]">
              <SelectValue placeholder="Genre" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Genres</SelectItem>
              {Object.values(MovieGenre).map((g) => (
                <SelectItem key={g} value={g}>
                  {g.charAt(0).toUpperCase() + g.slice(1).toLowerCase()}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <div className="flex justify-end">
          <Button variant="outline" onClick={handleReset} size="sm">
            Reset Filters
          </Button>
        </div>
        </div>
      </div>
    </div>
  );
};

export default MovieFilters;