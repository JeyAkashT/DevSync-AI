import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export type AppNotice = {
  id: string;
  message: string;
  variant: "info" | "error";
};

export type AppState = {
  title: string;
  notices: AppNotice[];
};

const initialState: AppState = {
  title: "DevSync AI",
  notices: [],
};

const appSlice = createSlice({
  name: "app",
  initialState,
  reducers: {
    pushNotice(state, action: PayloadAction<Omit<AppNotice, "id"> & { id?: string }>) {
      const id = action.payload.id ?? crypto.randomUUID();
      state.notices.push({
        id,
        message: action.payload.message,
        variant: action.payload.variant,
      });
    },
    dismissNotice(state, action: PayloadAction<string>) {
      state.notices = state.notices.filter((n) => n.id !== action.payload);
    },
  },
});

export const { pushNotice, dismissNotice } = appSlice.actions;
export const appReducer = appSlice.reducer;
