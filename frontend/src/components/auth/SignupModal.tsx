import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { motion, AnimatePresence } from "framer-motion";
import { X, Eye, EyeSlash } from "@phosphor-icons/react";
import { authApi } from "@/api/auth.api";
import { useAuthStore } from "@/store/authStore";
import { Button } from "@/components/ui/Button";
import { cn } from "@/utils/cn";
import toast from "react-hot-toast";

const schema = z
  .object({
    fullName: z.string().min(2, "Full name is required"),
    phone: z.string().min(7, "Phone number is required"),
    email: z.string().email("Invalid email address"),
    password: z.string().min(8, "Password must be at least 8 characters"),
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
type FormData = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onClose: () => void;
  onSwitchLogin: () => void;
}

export function SignupModal({ open, onClose, onSwitchLogin }: Props) {
  const { login } = useAuthStore();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  // Reset form state whenever the modal closes
  useEffect(() => {
    if (!open) {
      reset();
      setShowPassword(false);
      setShowConfirm(false);
    }
  }, [open, reset]);

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authApi.register({
        email: data.email,
        password: data.password,
        fullName: data.fullName,
        phoneNumber: data.phone,
      });
      const { accessToken, expiresIn, user } = res.data.data;
      login(accessToken, user, expiresIn ?? undefined);
      toast.success(
        `Welcome to Flash Food, ${user.fullName?.split(" ")[0] ?? "there"}!`,
      );
      onClose();
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? "Registration failed. Please try again.";
      toast.error(message);
    }
  };

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            key="overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40"
            onClick={onClose}
          />

          <motion.div
            key="modal"
            initial={{ opacity: 0, y: 32, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 16, scale: 0.96 }}
            transition={{ duration: 0.28, ease: "easeOut" }}
            className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-4"
          >
            <div className="bg-white rounded-t-2xl sm:rounded-2xl w-full max-w-sm shadow-xl border border-green-100 max-h-[92dvh] overflow-y-auto">
              {/* Header */}
              <div className="flex items-center justify-between p-6 pb-0">
                <div>
                  <h2 className="text-xl font-extrabold text-green-900">
                    Join Flash Food 🌿
                  </h2>
                  <p className="text-[0.8rem] text-green-600/70 mt-0.5">
                    Free to join. Start rescuing food tonight.
                  </p>
                </div>
                <button
                  onClick={onClose}
                  className="w-8 h-8 rounded-full bg-green-50 grid place-items-center hover:bg-green-100 transition-colors"
                >
                  <X size={14} weight="bold" className="text-green-600" />
                </button>
              </div>

              <form
                onSubmit={handleSubmit(onSubmit)}
                className="p-6 flex flex-col gap-4"
              >
                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">
                    Full Name
                  </label>
                  <input
                    {...register("fullName")}
                    type="text"
                    placeholder="Jane Doe"
                    autoComplete="name"
                    className={cn(
                      "w-full rounded-full border px-4 py-2.5 text-[0.9rem] outline-none transition",
                      "focus:ring-2 focus:ring-green-100",
                      errors.fullName
                        ? "border-red-300 focus:border-red-400 bg-red-50/40"
                        : "border-green-200 focus:border-green-500",
                    )}
                  />
                  {errors.fullName && (
                    <p className="text-red-500 text-[0.72rem] mt-1 pl-1">
                      {errors.fullName.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">
                    Phone Number
                  </label>
                  <input
                    {...register("phone")}
                    type="tel"
                    placeholder="+84 90 000 0000"
                    autoComplete="tel"
                    className={cn(
                      "w-full rounded-full border px-4 py-2.5 text-[0.9rem] outline-none transition",
                      "focus:ring-2 focus:ring-green-100",
                      errors.phone
                        ? "border-red-300 focus:border-red-400 bg-red-50/40"
                        : "border-green-200 focus:border-green-500",
                    )}
                  />
                  {errors.phone && (
                    <p className="text-red-500 text-[0.72rem] mt-1 pl-1">
                      {errors.phone.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">
                    Email
                  </label>
                  <input
                    {...register("email")}
                    type="email"
                    placeholder="you@example.com"
                    autoComplete="email"
                    className={cn(
                      "w-full rounded-full border px-4 py-2.5 text-[0.9rem] outline-none transition",
                      "focus:ring-2 focus:ring-green-100",
                      errors.email
                        ? "border-red-300 focus:border-red-400 bg-red-50/40"
                        : "border-green-200 focus:border-green-500",
                    )}
                  />
                  {errors.email && (
                    <p className="text-red-500 text-[0.72rem] mt-1 pl-1">
                      {errors.email.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">
                    Password
                  </label>
                  <div className="relative">
                    <input
                      {...register("password")}
                      type={showPassword ? "text" : "password"}
                      placeholder="Min. 8 characters"
                      autoComplete="new-password"
                      className={cn(
                        "w-full rounded-full border px-4 py-2.5 pr-11 text-[0.9rem] outline-none transition",
                        "focus:ring-2 focus:ring-green-100",
                        errors.password
                          ? "border-red-300 focus:border-red-400 bg-red-50/40"
                          : "border-green-200 focus:border-green-500",
                      )}
                    />
                    <button
                      type="button"
                      tabIndex={-1}
                      onClick={() => setShowPassword((v) => !v)}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-green-400 hover:text-green-600 transition-colors"
                      aria-label={
                        showPassword ? "Hide password" : "Show password"
                      }
                    >
                      {showPassword ? (
                        <EyeSlash size={16} weight="bold" />
                      ) : (
                        <Eye size={16} weight="bold" />
                      )}
                    </button>
                  </div>
                  {errors.password && (
                    <p className="text-red-500 text-[0.72rem] mt-1 pl-1">
                      {errors.password.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-[0.8rem] font-bold text-green-800 mb-1">
                    Confirm Password
                  </label>
                  <div className="relative">
                    <input
                      {...register("confirmPassword")}
                      type={showConfirm ? "text" : "password"}
                      placeholder="Repeat password"
                      autoComplete="new-password"
                      className={cn(
                        "w-full rounded-full border px-4 py-2.5 pr-11 text-[0.9rem] outline-none transition",
                        "focus:ring-2 focus:ring-green-100",
                        errors.confirmPassword
                          ? "border-red-300 focus:border-red-400 bg-red-50/40"
                          : "border-green-200 focus:border-green-500",
                      )}
                    />
                    <button
                      type="button"
                      tabIndex={-1}
                      onClick={() => setShowConfirm((v) => !v)}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-green-400 hover:text-green-600 transition-colors"
                      aria-label={
                        showConfirm ? "Hide password" : "Show password"
                      }
                    >
                      {showConfirm ? (
                        <EyeSlash size={16} weight="bold" />
                      ) : (
                        <Eye size={16} weight="bold" />
                      )}
                    </button>
                  </div>
                  {errors.confirmPassword && (
                    <p className="text-red-500 text-[0.72rem] mt-1 pl-1">
                      {errors.confirmPassword.message}
                    </p>
                  )}
                </div>

                <p className="text-[0.72rem] text-green-600/60 -mt-1">
                  By signing up you agree to our{" "}
                  <a href="#" className="text-green-600 hover:underline">
                    Terms
                  </a>{" "}
                  and{" "}
                  <a href="#" className="text-green-600 hover:underline">
                    Privacy Policy
                  </a>
                  .
                </p>

                <Button type="submit" className="w-full" loading={isSubmitting}>
                  {isSubmitting ? "Creating account…" : "Create Account"}
                </Button>

                <p className="text-center text-[0.8rem] text-green-600/70">
                  Already a rescuer?{" "}
                  <button
                    type="button"
                    onClick={onSwitchLogin}
                    className="text-green-600 font-bold hover:underline"
                  >
                    Sign in
                  </button>
                </p>
              </form>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
