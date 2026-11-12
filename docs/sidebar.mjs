const root = "rexcalendar";

export const rexcalendar = {
  label: "RexCalendar",
  items: [
    { label: "Overview", slug: root },
    {
      label: "Getting started",
      items: [
        { label: "Installation", slug: `${root}/getting-started/installation` },
        { label: "Requirements", slug: `${root}/getting-started/requirements` },
      ],
    },
    {
      label: "Configuration",
      items: [
        { label: "Settings", slug: `${root}/configuration/overview` },
        { label: "Messages", slug: `${root}/configuration/messages` },
        { label: "GUI", slug: `${root}/configuration/gui` },
        { label: "Rewards", slug: `${root}/configuration/rewards` },
        { label: "Storage", slug: `${root}/configuration/storage` },
      ],
    },
    {
      label: "Reference",
      items: [
        { label: "Commands", slug: `${root}/reference/commands` },
        { label: "Permissions", slug: `${root}/reference/permissions` },
      ],
    },
    {
      label: "Guides",
      items: [
        { label: "Days and timezone", slug: `${root}/guides/days-and-timezone` },
      ],
    },
    { label: "FAQ", slug: `${root}/faq` },
  ],
};
