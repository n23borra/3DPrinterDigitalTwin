import React, {useMemo, useState} from 'react';
import {Link, useLocation} from 'react-router-dom';
import clsx from 'clsx';

import styles from './Sidebar.module.css';

/**
 * Collapsible sidebar for dashboard navigation with profile and utility actions.
 *
 * @param {Object} props
 * @param {Array} props.menuItems
 * @param {string} props.appTitle
 * @param {{name: string, role: string, avatarUrl?: string}} props.user
 * @param {() => void} props.onLogout
 * @param {boolean} props.isDarkMode
 * @param {() => void} props.onToggleDarkMode
 * @param {string} [props.activePath]
 * @returns {JSX.Element}
 */
export default function Sidebar({
                                    menuItems,
                                    appTitle,
                                    user,
                                    onLogout,
                                    isDarkMode,
                                    onToggleDarkMode,
                                    activePath,
                                }) {
    const location = useLocation();
    const [isCollapsed, setIsCollapsed] = useState(false);
    const currentPath = activePath ?? location?.pathname ?? '';

    const initials = useMemo(() => {
        const name = user?.name ?? '';
        return name
            .split(' ')
            .filter(Boolean)
            .slice(0, 2)
            .map((part) => part[0]?.toUpperCase())
            .join('') || 'U';
    }, [user?.name]);

    const renderAvatar = () => {
        if (user?.avatarUrl) {
            return (
                <img
                    src={user.avatarUrl}
                    alt={`${user?.name || 'User'} avatar`}
                    className={styles.avatarImage}
                />
            );
        }

        return <div className={styles.avatarFallback}>{initials}</div>;
    };

    const handleItemAction = (item) => {
        if (item.onClick) item.onClick();
    };

    return (
        <aside className={clsx(styles.sidebar, isCollapsed && styles.collapsed)}>
            <div className={styles.topSection}>
                <div className={styles.titleRow}>
                    <span className={styles.title}>{appTitle}</span>
                    <button
                        type="button"
                        className={styles.collapseButton}
                        onClick={() => setIsCollapsed((prev) => !prev)}
                        aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
                    >
                        <span className={clsx(styles.collapseIcon, isCollapsed && styles.collapseIconCollapsed)}>
                            <svg viewBox="0 0 24 24" aria-hidden="true" className={styles.collapseSvg}>
                                <path d="M14.59 16.59L10 12l4.59-4.59L13.17 6l-6 6 6 6z" fill="currentColor" />
                            </svg>
                        </span>
                    </button>
                </div>

                <div className={styles.profileSection}>
                    <div className={styles.avatarWrapper}>{renderAvatar()}</div>
                    {!isCollapsed && (
                        <div className={styles.profileInfo}>
                            <div className={styles.userName}>{user?.name}</div>
                            <div className={styles.userRole}>{user?.role}</div>
                        </div>
                    )}
                </div>
            </div>

            <nav className={styles.menu} aria-label="Sidebar navigation">
                {menuItems.map((item) => {
                    const isActive = Boolean(item.path && currentPath.startsWith(item.path));
                    const Wrapper = item.path ? Link : 'button';
                    const wrapperProps = item.path
                        ? {to: item.path}
                        : {type: 'button', onClick: () => handleItemAction(item)};

                    return (
                        <div key={item.id} className={clsx(styles.menuItem, isActive && styles.active)}>
                            {isActive && <span className={styles.activeBar} aria-hidden="true" />}
                            <Wrapper {...wrapperProps} className={styles.menuLink}>
                                <span className={styles.icon}>{item.icon}</span>
                                {!isCollapsed && <span className={styles.label}>{item.label}</span>}
                            </Wrapper>
                        </div>
                    );
                })}
            </nav>

            <div className={styles.bottomSection}>
                <button type="button" onClick={onLogout} className={clsx(styles.menuLink, styles.logout)}>
                    <span className={styles.icon} aria-hidden="true">
                        <svg viewBox="0 0 24 24" className={styles.utilityIcon}>
                            <path
                                fill="currentColor"
                                d="M10 17l1.41-1.41L8.83 13H21v-2H8.83l2.58-2.59L10 7l-5 5 5 5zm-2 3h8a2 2 0 002-2V6a2 2 0 00-2-2H8a2 2 0 00-2 2v4h2V6h8v12H8v-4H6v4a2 2 0 002 2z"
                            />
                        </svg>
                    </span>
                    {!isCollapsed && <span className={styles.label}>Logout</span>}
                </button>

                <button
                    type="button"
                    onClick={onToggleDarkMode}
                    className={clsx(styles.menuLink, styles.darkMode)}
                    aria-pressed={isDarkMode}
                >
                    <div className={styles.darkModeLeft}>
                        <span className={styles.icon} aria-hidden="true">
                            <svg viewBox="0 0 24 24" className={styles.utilityIcon}>
                                <path
                                    fill="currentColor"
                                    d="M20 15.5A8.5 8.5 0 019.5 5c0-.34.02-.67.05-1A9.5 9.5 0 1021 14.45c-.33.03-.66.05-1 .05z"
                                />
                            </svg>
                        </span>
                        {!isCollapsed && <span className={styles.label}>Dark Mode</span>}
                    </div>
                    <span className={clsx(styles.switch, isDarkMode && styles.switchActive)}>
                        <span className={styles.switchThumb} />
                    </span>
                </button>
            </div>
        </aside>
    );
}