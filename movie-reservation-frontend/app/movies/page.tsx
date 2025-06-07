"use client";

import React, { useRef, useState } from "react";
import MovieList from "@/components/movies/MovieList";
import MovieFilters from "@/components/movies/MovieFilters";

export default function MoviesPage() {
  const [filters, setFilters] = useState<{ search: string; genre: string }>({ search: "", genre: "all" });

  const handleFilterChange = (newFilters: { search: string; genre: string }) => {
    setFilters(newFilters);
  };

  return (
    <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
      <div className="container py-8">
        <h1 className="text-3xl font-bold mb-6">Now Showing</h1>
        <MovieFilters onFilterChange={handleFilterChange} filters={filters} />
        <MovieList filters={filters} />
      </div>
    </section>
  );
}