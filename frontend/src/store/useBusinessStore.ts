import { create } from "zustand";
import { Business } from "@/types/api";

interface BusinessState {
  currentBusiness: Business | null;
  setCurrentBusiness: (business: Business | null) => void;
}

export const useBusinessStore = create<BusinessState>((set) => ({
  currentBusiness: null,
  setCurrentBusiness: (business) => set({ currentBusiness: business }),
}));
