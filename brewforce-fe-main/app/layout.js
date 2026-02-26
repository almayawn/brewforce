import { Poppins } from "next/font/google";
import "./globals.css";
import Footer from '../components/FooterCustom'; 
import Header from '../components/HeaderCustom'; 
import { Poppins } from 'next/font/google';

const poppins = Poppins({
  subsets: ['latin'],
  weight: ['300', '400', '500', '600', '700'],
  variable: '--font-poppins',
});

export const metadata = {
  title: "BrewForce Attack",
  description: "Discover mouthwatering food and drinks at unbeatable prices",
};

export default function Layout({ children }) {
  return (
    <>
      <html lang="en"  className={poppins.className}>
        <body className="bg-gray-100 text-gray-900">
           {/* Header */}
          <Header />
        

          {/* Main content */}
          <main className="flex-grow  bg-gradient-to-b from-amber-50 to-amber-100">{children}</main>

          {/* Footer */}
          <Footer />
        </body>
      </html>
    </>
  );
}