/****************************************************************************
** eweApplication meta object code from reading C++ file 'eweApplication.cpp'
**
** Created: Fri May 17 03:57:23 2002
**      by: The Qt MOC ($Id: qt/src/moc/moc.y   2.3.3   edited 2001-10-17 $)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

__IDSTRING(rcsid_qt2_ewe_app, "$MirOS: contrib/hosted/ewe/vm/qtEweApplication.cpp,v 1.1 2008/04/30 19:57:23 tg Exp $");

#if !defined(Q_MOC_OUTPUT_REVISION)
#define Q_MOC_OUTPUT_REVISION 9
#elif Q_MOC_OUTPUT_REVISION != 9
#error "Moc format conflict - please regenerate all moc files"
#endif

#include <qmetaobject.h>
#include <qapplication.h>



const char *eweApplication::className() const
{
    return "eweApplication";
}

QMetaObject *eweApplication::metaObj = 0;

void eweApplication::initMetaObject()
{
    if ( metaObj )
	return;
    if ( qstrcmp(QApplication::className(), "QApplication") != 0 )
	badSuperclassWarning("eweApplication","QApplication");
    (void) staticMetaObject();
}

#ifndef QT_NO_TRANSLATION

QString eweApplication::tr(const char* s)
{
    return qApp->translate( "eweApplication", s, 0 );
}

QString eweApplication::tr(const char* s, const char * c)
{
    return qApp->translate( "eweApplication", s, c );
}

#endif // QT_NO_TRANSLATION

QMetaObject* eweApplication::staticMetaObject()
{
    if ( metaObj )
	return metaObj;
    (void) QApplication::staticMetaObject();
#ifndef QT_NO_PROPERTIES
#endif // QT_NO_PROPERTIES
    typedef void (eweApplication::*m1_t0)();
    typedef void (QObject::*om1_t0)();
  	m1_t0 v1_0 = &eweApplication::tick;
    om1_t0 ov1_0 = (om1_t0)v1_0;
    QMetaData *slot_tbl = QMetaObject::new_metadata(1);
    QMetaData::Access *slot_tbl_access = QMetaObject::new_metaaccess(1);
    slot_tbl[0].name = "tick()";
    slot_tbl[0].ptr = (QMember)ov1_0;
    slot_tbl_access[0] = QMetaData::Public;
    metaObj = QMetaObject::new_metaobject(
	"eweApplication", "QApplication",
	slot_tbl, 1,
	0, 0,
#ifndef QT_NO_PROPERTIES
	0, 0,
	0, 0,
#endif // QT_NO_PROPERTIES
	0, 0 );
    metaObj->set_slot_access( slot_tbl_access );
#ifndef QT_NO_PROPERTIES
#endif // QT_NO_PROPERTIES
    return metaObj;
}
