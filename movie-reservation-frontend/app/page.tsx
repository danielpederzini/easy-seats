import Link from "next/link";
import { ArrowRight, Film, Calendar, CreditCard, Ticket } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function Home() {
  return (
    <div className="flex flex-col min-h-[calc(100vh-4rem)]">
      {/* Hero Section */}
      <section className="flex items-center justify-center w-full py-12 md:py-24 lg:py-32 xl:py-48 p-8 bg-black relative overflow-hidden">
        <div className="absolute inset-0 bg-[url('https://images.pexels.com/photos/7991579/pexels-photo-7991579.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260')] bg-cover bg-center opacity-30"></div>
        <div className="container px-4 md:px-6 relative z-10">
          <div className="flex flex-col items-center justify-center space-y-4 text-center">
            <div className="space-y-2 mb-10">
              <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl lg:text-6xl/none text-white">
                Book Your Cinema Experience
              </h1>
              <p className="mx-auto max-w-[700px] text-gray-300 md:text-xl">
                Reserve the best seats for the latest movies with our easy-to-use booking system.
              </p>
            </div>
            <div className="space-x-4">
              <Button asChild size="lg" className="animate-pulse">
                <Link href="/movies">
                  Browse Movies <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="flex items-center justify-center w-full py-12 md:py-24 lg:py-32 p-8 bg-black">
        <div className="container px-4 md:px-6">
          <div className="flex flex-col items-center justify-center space-y-4 text-center">
            <div className="space-y-2 mb-10">
              <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">
                Simple Booking Process
              </h2>
              <p className="mx-auto max-w-[700px] text-muted-foreground md:text-xl">
                Book your favorite movies in just a few clicks.
              </p>
            </div>
            <div className="mx-auto grid max-w-5xl grid-cols-1 gap-6 md:grid-cols-3 lg:gap-12">
              <div className="flex flex-col items-center space-y-4">
                <div className="rounded-full p-4 bg-primary/10">
                  <Film className="h-10 w-10 text-primary" />
                </div>
                <h3 className="text-xl font-bold">Choose Your Movie</h3>
                <p className="text-muted-foreground text-center">
                  Browse through our selection of the latest and greatest films.
                </p>
              </div>
              <div className="flex flex-col items-center space-y-4">
                <div className="rounded-full p-4 bg-primary/10">
                  <Calendar className="h-10 w-10 text-primary" />
                </div>
                <h3 className="text-xl font-bold">Select Your Seats</h3>
                <p className="text-muted-foreground text-center">
                  Choose the perfect seats from our interactive seating plan.
                </p>
              </div>
              <div className="flex flex-col items-center space-y-4">
                <div className="rounded-full p-4 bg-primary/10">
                  <Ticket className="h-10 w-10 text-primary" />
                </div>
                <h3 className="text-xl font-bold">Enjoy the Show</h3>
                <p className="text-muted-foreground text-center">
                  Receive your tickets and get ready for a fantastic cinema experience.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="flex items-center justify-center w-full py-12 md:py-24 lg:py-32 p-8 bg-black">
        <div className="container px-4 md:px-6">
          <div className="flex flex-col items-center justify-center space-y-4 text-center">
            <div className="space-y-2 mb-10">
              <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">
                Ready to Book Your Next Movie?
              </h2>
              <p className="mx-auto max-w-[700px] text-muted-foreground md:text-xl">
                Start browsing our available movies and showtimes now.
              </p>
            </div>
            <div className="space-x-4">
              <Button asChild size="lg">
                <Link href="/auth/signup">
                  Sign Up Now <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}