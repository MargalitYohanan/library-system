import React from 'react';
import DOMPurify from 'dompurify'; 
import './NotificationItem.css';

export default function NotificationItem({ notification, compact = false }) {
  
  const rawContent = notification.contentNotification || notification.content;
  const contentText =
    typeof rawContent === "string"
      ? rawContent
      : rawContent?.contentNotification || "";
  const safeHTML = DOMPurify.sanitize(contentText);

  const formatFriendlyDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('he-IL', {
      day: 'numeric',
      month: 'numeric',
      year: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const classNames = [
    'notification-item',
    notification.isRead ? 'notification-item--read' : 'notification-item--unread',
    compact ? 'notification-item--compact' : '',
  ].join(' ').trim();

  return (
    <div className={classNames}>
      <div
        className="notification-item__content"
        dangerouslySetInnerHTML={{ __html: safeHTML }}
      />

      <small className="notification-item__date">
        {formatFriendlyDate(notification.sendDate)}
      </small>
    </div>
  );
}