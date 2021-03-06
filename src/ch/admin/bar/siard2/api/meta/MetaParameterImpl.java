/*== MetaParameterImpl.java ============================================
MetaParameterImpl implements the interface MetaParameter.
Application : SIARD 2.0
Description : MetaParameterImpl implements the interface MetaParameter.
------------------------------------------------------------------------
Copyright  : Swiss Federal Archives, Berne, Switzerland, 2016
Created    : 29.06.2016, Hartwig Thomas, Enter AG, Rüti ZH
======================================================================*/
package ch.admin.bar.siard2.api.meta;

import java.io.*;
import java.math.*;
import ch.enterag.utils.*;
import ch.enterag.utils.xml.XU;
import ch.enterag.sqlparser.*;
import ch.enterag.sqlparser.datatype.*;
import ch.enterag.sqlparser.datatype.enums.*;
import ch.enterag.sqlparser.ddl.enums.*;
import ch.admin.bar.siard2.api.*;
import ch.admin.bar.siard2.api.generated.*;
import ch.admin.bar.siard2.api.primary.*;

/*====================================================================*/
/** MetaParameterImpl implements the interface MetaParameter.
 @author Hartwig Thomas
 */
public class MetaParameterImpl
  extends MetaSearchImpl
  implements MetaParameter
{
  private MetaRoutine _mr = null;
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public MetaRoutine getParentMetaRoutine() { return _mr; }
  
  ParameterType _pt = null;
  public ParameterType getParameterType()
    throws IOException
  {
    return _pt;
  } /* getParameterType */

  private int _iPosition = -1;
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public int getPosition() { return _iPosition; }
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public boolean isValid() { return (getType() != null) || (getTypeName() != null); }
  
  /*------------------------------------------------------------------*/
  /** get archive
   * @return archive.
   */
  private ArchiveImpl getArchiveImpl()
  {
    return (ArchiveImpl)getParentMetaRoutine().getParentMetaSchema().getSchema().getParentArchive();
  } /* getArchive */
  
  private ParameterType _ptTemplate = null;
  /*------------------------------------------------------------------*/
  /** set template meta data.
   * @param ptTemplate template data.
   */
  public void setTemplate(ParameterType ptTemplate)
    throws IOException
  {
    _ptTemplate = ptTemplate;
    if (!SU.isNotEmpty(getDescription()))
      setDescription(XU.fromXml(_ptTemplate.getDescription()));
  } /* setTemplate */
  
  /*------------------------------------------------------------------*/
  /** constructor
   * @param mr parent routine meta data object of SIARD archive.
   * @param pt ParameterType instance (JAXB).
   * @param iPosition position (1-based) of parameter in routine.
   * @throws IOException if an I/O error occurred.
   */
  private MetaParameterImpl(MetaRoutine mr, ParameterType pt, int iPosition)
    throws IOException
  {
    _mr = mr;
    _pt = pt;
    _iPosition = iPosition;
  } /* constructor MetaParameterImpl */
  
  /*------------------------------------------------------------------*/
  /** factory
   * @param mr parent routine meta data object of SIARD archive.
   * @param pt ParameterType instance (JAXB).
   * @param iPosition position (1-based) of parameter in routine.
   * @return new MetaParameter instance.
   * @throws IOException if an I/O error occurred.
   */
  public static MetaParameter newInstance(MetaRoutine mr, ParameterType pt, int iPosition)
    throws IOException
  {
    return new MetaParameterImpl(mr,pt,iPosition);
  } /* newInstance */

  /* property Name */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getName() { return XU.fromXml(_pt.getName()); }
  
  /* property Mode */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setMode(String sMode)
    throws IOException
  {
    Archive archive = getArchiveImpl();
    if (archive.canModifyPrimaryData())
    {
      sMode = sMode.toUpperCase();
      ParameterMode pm = ParameterMode.getByKeywords(sMode);
      if (pm != null)
      {
        if (getArchiveImpl().isMetaDataDifferent(getMode(),pm.getKeywords()))
          _pt.setMode(sMode);
      }
      else
        throw new IllegalArgumentException("Mode must be IN, OUT or INOUT!");
    }
    else
      throw new IOException("Mode cannot be set!");
  } /* setMode */ 
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getMode() { return _pt.getMode(); }
  
  /* property Type */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setType(String sType)
    throws IOException
  {
    if (getArchiveImpl().canModifyPrimaryData())
    {
      if (getArchiveImpl().isMetaDataDifferent(getTypeSchema(),null))
        _pt.setTypeSchema(null);
      if (getArchiveImpl().isMetaDataDifferent(getTypeName(),null))
        _pt.setTypeName(null);
      SqlFactory sf = new BaseSqlFactory();
      PredefinedType pt = new PredefinedType(sf);
      pt.parse(sType);
      sType = pt.format();
      if (getArchiveImpl().isMetaDataDifferent(getType(),sType))
        _pt.setType(XU.toXml(sType));
    }
    else
      throw new IOException("Type cannot be set!");
  } /* setType */ 
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setPreType(int iDataType, long lPrecision, int iScale)
    throws IOException
  {
    SqlFactory sf = new BaseSqlFactory();
    PredefinedType prt = sf.newPredefinedType();
    prt.initialize(iDataType, lPrecision, iScale);
    String sType = prt.format();
    setType(sType);
  } /* setPreType */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getType() { return XU.fromXml(_pt.getType()); }
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public int getPreType()
  {
    int iDataType = java.sql.Types.NULL;
    MetaType mt = getMetaType();
    CategoryType cat = null;
    if (mt != null)
      cat = mt.getCategoryType();
    String sType = getType();
    if (sType != null)
    {
      SqlFactory sf = new BaseSqlFactory();
      PredefinedType prt = sf.newPredefinedType();
      prt.parse(sType);
      PreType pt = prt.getType();
      iDataType = pt.getSqlType();
    }
    else if (cat == CategoryType.DISTINCT)
      iDataType = mt.getBasePreType();
    return iDataType;
  } /* getPreType */
  
  /* property TypeOriginal */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setTypeOriginal(String sTypeOriginal)
    throws IOException
  {
    Archive archive = getArchiveImpl();
    if (archive.canModifyPrimaryData() && (getTypeName() == null))
    {
      if (getArchiveImpl().isMetaDataDifferent(getTypeOriginal(),sTypeOriginal))
        _pt.setTypeOriginal(XU.toXml(sTypeOriginal));
    }
    else
      throw new IOException("Original type cannot be set!");
  } /* setTypeOriginal */ 
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getTypeOriginal() { return XU.fromXml(_pt.getTypeOriginal()); }

  /* property TypeSchema */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setTypeSchema(String sTypeSchema)
    throws IOException
  {
    if (getArchiveImpl().canModifyPrimaryData())
    {
      if (getArchiveImpl().isMetaDataDifferent(getType(),null))
        _pt.setType(null);
      if (getArchiveImpl().isMetaDataDifferent(getTypeSchema(),sTypeSchema))
        _pt.setTypeSchema(XU.toXml(sTypeSchema));
    }
    else
      throw new IOException("Type schema cannot be set!");
  } /* setTypeSchema */ 
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getTypeSchema() { return XU.fromXml(_pt.getTypeSchema()); }
  
  /* property TypeName */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setTypeName(String sTypeName)
    throws IOException
  {
    if (getArchiveImpl().canModifyPrimaryData())
    {
      if (getArchiveImpl().isMetaDataDifferent(getType(),null))
        _pt.setType(null);
      if (getArchiveImpl().isMetaDataDifferent(getTypeName(),sTypeName))
      {
        _pt.setTypeName(XU.toXml(sTypeName));
        if (getTypeSchema() == null)
          setTypeSchema(getParentMetaRoutine().getParentMetaSchema().getName());
      }
    }
    else
      throw new IOException("Type name cannot be set!");
  } /* setTypeName */ 
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getTypeName() { return XU.fromXml(_pt.getTypeName()); }
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override 
  public MetaType getMetaType()
  {
    MetaType mt = null;
    if (getTypeName() != null)
    {
      Schema schema = getArchiveImpl().getSchema(getTypeSchema()); 
      mt = schema.getMetaSchema().getMetaType(getTypeName());
    }
    return mt;
  } /* getMetaType */
  
  /* property Cardinality */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public void setCardinality(int iCardinality)
    throws IOException
  {
    if (getArchiveImpl().canModifyPrimaryData())
    {
      if (getArchiveImpl().isMetaDataDifferent(getCardinality(),iCardinality))
        _pt.setCardinality(BigInteger.valueOf((long)iCardinality));
    }
    else
      throw new IOException("Cardinality cannot be set!");
  } /* setCardinality */
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public int getCardinality()
  {
    int iCardinality = -1;
    BigInteger bi = _pt.getCardinality();
    if (bi != null)
      iCardinality = bi.intValue();
    return iCardinality;
  } /* getCardinality */
  
  /* property Description */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override 
  public void setDescription(String sDescription)
  { 
    if (getArchiveImpl().isMetaDataDifferent(getDescription(),sDescription))
      _pt.setDescription(XU.toXml(sDescription)); 
  } /* setDescription */
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String getDescription() { return XU.fromXml(_pt.getDescription()); }
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override public String[] getSearchElements(DU du)
    throws IOException
  { 
    return new String[] 
      {
        getName(),
        String.valueOf(getPosition()),
        getMode(),
        getType(),
        getTypeSchema(),
        getTypeName(),
        getTypeOriginal(),
        (getCardinality() <= 0)? "": String.valueOf(getCardinality()),
        getDescription()
      };
  } /* getSearchElements */
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc}
   * toString() returns the name of the parameter which is to be displayed 
   * as the label of the parameter node of the tree displaying the archive.   
   */
  @Override 
  public String toString()
  {
    return getName();
  }
} /* class MetaParameterImpl */
