import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";

import { fetchMeApi, loginApi, registerApi } from "./authApi";
import type { AuthUser } from "./types";
import {
  clearAccessToken,
  persistAccessToken,
  readAccessToken,
} from "./session";

export type AuthState = {
  token: string | null;
  user: AuthUser | null;
  status: "idle" | "loading" | "succeeded" | "failed";
  error: string | null;
};

const initialState: AuthState = {
  token: readAccessToken(),
  user: null,
  status: "idle",
  error: null,
};

export const login = createAsyncThunk(
  "auth/login",
  async (body: { email: string; password: string }, { rejectWithValue }) => {
    try {
      return await loginApi(body);
    } catch (e: unknown) {
      return rejectWithValue(e);
    }
  }
);

export const register = createAsyncThunk(
  "auth/register",
  async (
    body: { email: string; password: string; fullName?: string },
    { rejectWithValue }
  ) => {
    try {
      return await registerApi(body);
    } catch (e: unknown) {
      return rejectWithValue(e);
    }
  }
);

export const loadCurrentUser = createAsyncThunk(
  "auth/me",
  async (_, { rejectWithValue }) => {
    try {
      return await fetchMeApi();
    } catch (e: unknown) {
      return rejectWithValue(e);
    }
  }
);

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout(state) {
      state.token = null;
      state.user = null;
      state.status = "idle";
      state.error = null;
      clearAccessToken();
    },
    clearError(state) {
      state.error = null;
    },
    sessionRestored(state, action: PayloadAction<{ token: string | null }>) {
      state.token = action.payload.token;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload.accessToken;
        state.user = action.payload.user;
        persistAccessToken(action.payload.accessToken);
      })
      .addCase(login.rejected, (state) => {
        state.status = "failed";
      })
      .addCase(register.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.token = action.payload.accessToken;
        state.user = action.payload.user;
        persistAccessToken(action.payload.accessToken);
      })
      .addCase(register.rejected, (state) => {
        state.status = "failed";
      })
      .addCase(loadCurrentUser.pending, (state) => {
        state.status = "loading";
      })
      .addCase(loadCurrentUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload;
      })
      .addCase(loadCurrentUser.rejected, (state) => {
        state.status = "failed";
        state.token = null;
        state.user = null;
        clearAccessToken();
      });
  },
});

export const { logout, clearError, sessionRestored } = authSlice.actions;

export function selectToken(state: { auth: AuthState }): string | null {
  return state.auth.token;
}

export function selectAuthUser(state: { auth: AuthState }): AuthUser | null {
  return state.auth.user;
}

export function selectAuthLoading(state: { auth: AuthState }): boolean {
  return state.auth.status === "loading";
}

export const authReducer = authSlice.reducer;
