// Same values as the backend's JobType enum.
export const JOB_TYPES = [
  { value: "FULL_TIME", label: "Full time" },
  { value: "PART_TIME", label: "Part time" },
  { value: "INTERNSHIP", label: "Internship" },
  { value: "CONTRACT", label: "Contract" },
];

export function jobTypeLabel(value) {
  return JOB_TYPES.find((type) => type.value === value)?.label ?? value;
}
